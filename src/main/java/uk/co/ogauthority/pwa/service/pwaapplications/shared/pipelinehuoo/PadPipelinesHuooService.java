package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickHuooPipelineValidationType;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickHuooPipelinesFormValidator;

@Service
public class PadPipelinesHuooService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PadPipelinesHuooService.class);

  private final PickableHuooPipelineService pickableHuooPipelineService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final PickHuooPipelinesFormValidator pickHuooPipelinesFormValidator;
  private final PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository;

  @Autowired
  public PadPipelinesHuooService(
      PickableHuooPipelineService pickableHuooPipelineService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PadOrganisationRoleService padOrganisationRoleService,
      PickHuooPipelinesFormValidator pickHuooPipelinesFormValidator,
      PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository) {
    this.pickableHuooPipelineService = pickableHuooPipelineService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.pickHuooPipelinesFormValidator = pickHuooPipelinesFormValidator;
    this.padPipelineOrganisationRoleLinkRepository = padPipelineOrganisationRoleLinkRepository;
  }


  public void validateAddPipelineHuooForm(PwaApplicationDetail pwaApplicationDetail,
                                          PickHuooPipelinesForm form,
                                          BindingResult bindingResult,
                                          PickHuooPipelineValidationType pickHuooPipelineValidationType,
                                          HuooRole huooRole) {

    var availableTreatiesForRole = getAvailableTreatyAgreementsForRole(pwaApplicationDetail, huooRole);
    pickHuooPipelinesFormValidator.validate(form,
        bindingResult, List.of(
            huooRole,
            pwaApplicationDetail,
            pickHuooPipelineValidationType
        ).toArray());

  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    LOGGER.info("is complete not implemented");
    return false;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    LOGGER.info("validate not implemented");
    return bindingResult;
  }


  /* Convenience method passing args to the actual pad org roles service */
  public List<PadOrganisationRole> getPadOrganisationRolesFrom(PwaApplicationDetail pwaApplicationDetail,
                                                               HuooRole huooRole,
                                                               Set<OrganisationUnitId> organisationUnitIds,
                                                               Set<TreatyAgreement> treatyAgreements) {

    return padOrganisationRoleService.getOrgRolesForDetailByRole(
        pwaApplicationDetail,
        huooRole
    )
        .stream()
        // only return roles where the role is one of the chosen treaties or organisation units.
        .filter(padOrganisationRole -> {
          if (padOrganisationRole.getType().equals(HuooType.TREATY_AGREEMENT)) {
            return treatyAgreements.contains(padOrganisationRole.getAgreement());
          }
          return organisationUnitIds.contains(OrganisationUnitId.from(padOrganisationRole.getOrganisationUnit()));
        })
        .collect(toList());

  }

  @Transactional
  public void updatePipelineHuooLinks(PwaApplicationDetail pwaApplicationDetail,
                                      Set<PipelineIdentifier> pipelineIdentifiers,
                                      HuooRole huooRole,
                                      Set<OrganisationUnitId> organisationsToLink,
                                      Set<TreatyAgreement> treatiesToLink) {

    padOrganisationRoleService.deletePadPipelineRoleLinksForPipelineIdentifiersAndRole(
        pwaApplicationDetail,
        pipelineIdentifiers,
        huooRole
    );

    var organisationRoles = getPadOrganisationRolesFrom(
        pwaApplicationDetail,
        huooRole,
        organisationsToLink,
        treatiesToLink);

    // This is probably ok...batch inserts aren't really possible atm due to using IDENTITY primary key columns.
    for (PadOrganisationRole padOrganisationRole : organisationRoles) {
      for (PipelineIdentifier pipelineIdentifier : pipelineIdentifiers) {
        padOrganisationRoleService.createPadPipelineOrganisationRoleLink(padOrganisationRole, pipelineIdentifier);
      }
    }

  }

  public List<PickableHuooPipelineOption> getSortedPickablePipelineOptionsForApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole) {
    return pickableHuooPipelineService
        .getAllPickablePipelinesForApplicationAndRole(pwaApplicationDetail, huooRole)
        .stream()
        .sorted(Comparator.comparing(PickableHuooPipelineOption::getPipelineNumber)
            .thenComparing(o -> StringUtils.defaultIfEmpty(o.getSplitInfo(), ""))
        )
        .collect(toList());
  }


  /**
   * Given pipeline ids return successfully reconciled pickable pipelines for the application detail.
   */
  public Set<ReconciledHuooPickablePipeline> reconcilePickablePipelinesFromPipelineIds(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Set<String> pickedPipelineStringIds) {

    var pickedPipelineIds = pickedPipelineStringIds.stream()
        .map(PickableHuooPipelineId::from)
        .collect(toSet());

    // Pickable pipelines are not guaranteed to have the actual PipelineId available depending on app or consented model source.
    // reconcile to match pipelineId to arguments and filter out any invalid pipelines
    return pickableHuooPipelineService.reconcilePickablePipelineIds(
        pwaApplicationDetail,
        huooRole,
        pickedPipelineIds
    )
        .stream()
        .filter(rpp -> pickedPipelineStringIds.contains(rpp.getPickableIdAsString()))
        .collect(Collectors.toSet());
  }

  /**
   * Helper to convert sets of raw org unit ids and treaties into single set of roleOwner dtos.
   */
  private Set<OrganisationRoleOwnerDto> createRoleOwnersFrom(Set<Integer> organisationUnitIds,
                                                             Set<TreatyAgreement> treatyAgreements) {

    var orgUnitRoleOwners = organisationUnitIds.stream()
        .map(o -> OrganisationRoleOwnerDto.fromOrganisationUnitId(new OrganisationUnitId(o)))
        .collect(Collectors.toSet());

    var orgTreatyRoleOwners = treatyAgreements.stream()
        .map(OrganisationRoleOwnerDto::fromTreaty)
        .collect(Collectors.toSet());

    return SetUtils.union(orgUnitRoleOwners, orgTreatyRoleOwners);
  }

  /**
   * We want to turn raw org unit ids and treaty agreements and reconcile those arguments with valid org roles for an application detail.
   */
  public Set<OrganisationRoleOwnerDto> reconcileOrganisationRoleOwnersFrom(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Set<Integer> organisationUnitIds,
      Set<TreatyAgreement> treatyAgreements) {

    var searchOrgRoleOwners = createRoleOwnersFrom(organisationUnitIds, treatyAgreements);

    var allOrgRoleInstancesForRole = padOrganisationRoleService.getOrganisationRoleInstanceDtosByRole(
        pwaApplicationDetail, huooRole);

    return allOrgRoleInstancesForRole.stream()
        .filter(o -> searchOrgRoleOwners.contains(o.getOrganisationRoleOwnerDto()))
        .map(OrganisationRoleInstanceDto::getOrganisationRoleOwnerDto)
        .collect(Collectors.toSet());
  }

  public List<OrganisationUnitDetailDto> getAvailableOrgUnitDetailsForRole(PwaApplicationDetail pwaApplicationDetail,
                                                                           HuooRole huooRole) {
    var orgUnitsForRole = padOrganisationRoleService.getOrgRolesForDetail(pwaApplicationDetail)
        .stream()
        .filter(o -> huooRole.equals(o.getRole()))
        .map(PadOrganisationRole::getOrganisationUnit)
        .collect(toList());

    return portalOrganisationsAccessor.getOrganisationUnitDetailDtos(orgUnitsForRole);

  }

  public List<TreatyAgreement> getAvailableTreatyAgreementsForRole(PwaApplicationDetail pwaApplicationDetail,
                                                                   HuooRole huooRole) {
    return padOrganisationRoleService.getOrgRolesForDetail(pwaApplicationDetail)
        .stream()
        .filter(o -> huooRole.equals(o.getRole()))
        .filter(o -> HuooType.TREATY_AGREEMENT.equals(o.getType()))
        .map(PadOrganisationRole::getAgreement)
        .sorted(Comparator.comparing(TreatyAgreement::getAgreementText))
        .collect(toList());
  }

  public PipelineAndOrganisationRoleGroupSummaryDto createPipelineAndOrganisationRoleGroupSummary(
      PwaApplicationDetail pwaApplicationDetail) {

    var allPipelineRolesForApp = padPipelineOrganisationRoleLinkRepository.findOrganisationPipelineRoleDtoByPwaApplicationDetail(
        pwaApplicationDetail);
    return PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(allPipelineRolesForApp);
  }


}
