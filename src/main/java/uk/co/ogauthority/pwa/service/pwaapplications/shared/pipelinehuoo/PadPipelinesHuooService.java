package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PadPipelineHuooViewFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineAndOrgRoleGroupViewsByRole;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooValidationResult;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactory;
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
  private final PipelineAndIdentViewFactory pipelineAndIdentViewFactory;
  private final PadPipelineHuooViewFactory padPipelineHuooViewFactory;
  private final PadOptionConfirmedService padOptionConfirmedService;

  @Autowired
  public PadPipelinesHuooService(PickableHuooPipelineService pickableHuooPipelineService,
                                 PortalOrganisationsAccessor portalOrganisationsAccessor,
                                 PadOrganisationRoleService padOrganisationRoleService,
                                 PickHuooPipelinesFormValidator pickHuooPipelinesFormValidator,
                                 PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository,
                                 PipelineAndIdentViewFactory pipelineAndIdentViewFactory,
                                 PadPipelineHuooViewFactory padPipelineHuooViewFactory,
                                 PadOptionConfirmedService padOptionConfirmedService) {
    this.pickableHuooPipelineService = pickableHuooPipelineService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.pickHuooPipelinesFormValidator = pickHuooPipelinesFormValidator;
    this.padPipelineOrganisationRoleLinkRepository = padPipelineOrganisationRoleLinkRepository;
    this.pipelineAndIdentViewFactory = pipelineAndIdentViewFactory;
    this.padPipelineHuooViewFactory = padPipelineHuooViewFactory;
    this.padOptionConfirmedService = padOptionConfirmedService;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    // do not do additional type checks as this is covered by the controller markup
    return !PwaApplicationType.OPTIONS_VARIATION.equals(pwaApplicationDetail.getPwaApplicationType())
        || padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail);
  }


  public PipelineAndOrgRoleGroupViewsByRole getPadPipelinesHuooSummaryView(PwaApplicationDetail pwaApplicationDetail) {

    var pipelineAndOrgGroupAppSummary = createPipelineAndOrganisationRoleGroupSummary(pwaApplicationDetail);

    return padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(pwaApplicationDetail, pipelineAndOrgGroupAppSummary);

  }

  public PipelineHuooValidationResult generatePipelineHuooValidationResult(
      PwaApplicationDetail pwaApplicationDetail,
      PipelineAndOrgRoleGroupViewsByRole pipelineAndOrgRoleGroupViewsByRole) {

    return new PipelineHuooValidationResult(
        pipelineAndOrgRoleGroupViewsByRole,
        pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(
            pwaApplicationDetail,
            PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES)
    );
  }

  public Optional<PipelineOverview> getSplitablePipelineOverviewForApplication(
      PwaApplicationDetail pwaApplicationDetail,
      PipelineId pipelineId) {
    return getSplitablePipelinesForAppAndMasterPwa(pwaApplicationDetail)
        .stream()
        .filter(pipelineOverview -> pipelineId.equals(PipelineId.from(pipelineOverview)))
        .findFirst();
  }

  public List<PipelineOverview> getSplitablePipelinesForAppAndMasterPwa(PwaApplicationDetail pwaApplicationDetail) {
    return pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES
    )
        .values()
        .stream()
        .filter(pipelineOverview -> pipelineOverview.getNumberOfIdents() >= 1)
        .collect(Collectors.toUnmodifiableList());
  }

  public PipelineOverview getSplitablePipelineForAppAndMasterPwaOrError(PwaApplicationDetail pwaApplicationDetail,
                                                                        PipelineId pipelineId) {
    return getSplitablePipelineOverviewForApplication(pwaApplicationDetail, pipelineId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Splitable pipeline not found. pipeline id: " + pipelineId.asInt())
        );
  }

  public long countDistinctRoleOwnersForRole(PwaApplicationDetail pwaApplicationDetail, HuooRole huooRole) {
    return padOrganisationRoleService.getAssignableOrganisationRoleInstanceDtosByRole(pwaApplicationDetail, huooRole)
        .stream()
        .map(OrganisationRoleInstanceDto::getOrganisationRoleOwnerDto)
        .distinct()
        .count();
  }

  @Transactional
  public void removeSplitsForPipeline(PwaApplicationDetail pwaApplicationDetail,
                                      PipelineId pipelineId,
                                      HuooRole huooRole) {
    removeSplitsForPipelineWithinRole(pwaApplicationDetail, pipelineId, huooRole);
  }

  private boolean doesRoleHavePipelineLinks(PadOrganisationRole padOrganisationRole) {
    return padPipelineOrganisationRoleLinkRepository.countByPadOrgRole(padOrganisationRole) > 0L;
  }

  public void validateAddPipelineHuooForm(PwaApplicationDetail pwaApplicationDetail,
                                          PickHuooPipelinesForm form,
                                          BindingResult bindingResult,
                                          PickHuooPipelineValidationType pickHuooPipelineValidationType,
                                          HuooRole huooRole) {

    pickHuooPipelinesFormValidator.validate(
        form,
        bindingResult, List.of(
            huooRole,
            pwaApplicationDetail,
            pickHuooPipelineValidationType
        ).toArray());

  }

  @Override
  public boolean isComplete(PwaApplicationDetail pwaApplicationDetail) {
    var pipelineHuooSummary = getPadPipelinesHuooSummaryView(pwaApplicationDetail);
    var validationResult = generatePipelineHuooValidationResult(pwaApplicationDetail, pipelineHuooSummary);

    return validationResult.isValid();
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    LOGGER.warn("Validate method should not be called");
    return bindingResult;
  }

  /* Convenience method passing args to the actual pad org roles service */
  public List<PadOrganisationRole> getAssignablePadOrganisationRolesFrom(PwaApplicationDetail pwaApplicationDetail,
                                                                         HuooRole huooRole,
                                                                         Set<OrganisationUnitId> organisationUnitIds,
                                                                         Set<TreatyAgreement> treatyAgreements) {
    return padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(
        pwaApplicationDetail,
        huooRole
    )
        .stream()
        // exclude huooType where we usually dont want any role to be be assigned
        .filter(padOrganisationRole -> !HuooType.UNASSIGNED_PIPELINE_SPLIT.equals(padOrganisationRole.getType()))
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

    var organisationRoles = getAssignablePadOrganisationRolesFrom(
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

    var allOrgRoleInstancesForRole = padOrganisationRoleService.getAssignableOrganisationRoleInstanceDtosByRole(
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

    LOGGER.debug("Found {} org pipeline roles for detail", allPipelineRolesForApp.size());

    return PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(allPipelineRolesForApp);
  }

  @Transactional
  public List<PadPipelineOrganisationRoleLink> replacePipelineSectionsForPipelineAndRole(PwaApplicationDetail pwaApplicationDetail,
                                                                                         HuooRole huooRole,
                                                                                         PipelineId pipelineId,
                                                                                         List<PipelineSection> pipelineSections) {

    removeSplitsForPipelineWithinRole(pwaApplicationDetail, pipelineId, huooRole);
    var unassignedPipelineSplitRole = padOrganisationRoleService.getOrCreateUnassignedPipelineSplitRole(
        pwaApplicationDetail,
        huooRole);

    var unassignedPipelineSplitRoles = new ArrayList<PadPipelineOrganisationRoleLink>();
    pipelineSections.forEach(pipelineSection -> {
      var pipelineSectionRoleLink = new PadPipelineOrganisationRoleLink();
      pipelineSectionRoleLink.setPadOrgRole(unassignedPipelineSplitRole);
      pipelineSectionRoleLink.visit(pipelineSection);
      unassignedPipelineSplitRoles.add(pipelineSectionRoleLink);
    });

    return IterableUtils.toList(padPipelineOrganisationRoleLinkRepository.saveAll(unassignedPipelineSplitRoles));

  }

  // helper containing shared "remove splits for pipeline" logic required by seperate @public Tranactional methods.
  private void removeSplitsForPipelineWithinRole(PwaApplicationDetail pwaApplicationDetail,
                                                 PipelineId pipelineId,
                                                 HuooRole huooRole) {

    var splitPipelineRoles = padPipelineOrganisationRoleLinkRepository
        .findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
            pwaApplicationDetail, huooRole, Set.of(pipelineId.asInt())
        );

    var temporarySplitRoleOptional = splitPipelineRoles.stream()
        .map(PadPipelineOrganisationRoleLink::getPadOrgRole)
        .filter(padOrganisationRole -> HuooType.UNASSIGNED_PIPELINE_SPLIT.equals(padOrganisationRole.getType()))
        .findFirst();

    padOrganisationRoleService.removalPipelineOrgRoleLinks(splitPipelineRoles);
    // remove temporary role if no pipeline links remain after processing

    temporarySplitRoleOptional.ifPresent(role -> {
      if (!doesRoleHavePipelineLinks(role)) {
        padOrganisationRoleService.removeOrgRole(role);
      }
    });

  }

  /**
   * Pipeline link data copied as part of HUOO data copy.
   * See {@link uk.co.ogauthority.pwa.features.application.tasks.huoo.PadHuooTaskSectionService#copySectionInformation}
   */
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // do nothing as pipeline org link copied elsewhere
  }

  public List<PadPipelineOrganisationRoleLink> getPadPipelineOrgRoleLinksForDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineOrganisationRoleLinkRepository.getAllByPadOrgRole_PwaApplicationDetail(pwaApplicationDetail);
  }

}
