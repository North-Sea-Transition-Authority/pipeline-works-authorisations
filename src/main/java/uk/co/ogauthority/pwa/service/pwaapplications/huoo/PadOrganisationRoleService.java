package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.huoo.AddHuooController;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.OrganisationRolePipelineGroupDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.OrganisationRolesSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.enums.pipelinehuoo.OrgRoleInstanceType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.HuooOrganisationUnitRoleView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.HuooTreatyAgreementView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.validators.huoo.HuooValidationView;

@Service
public class PadOrganisationRoleService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PadOrganisationRoleService.class);

  private final PadOrganisationRolesRepository padOrganisationRolesRepository;
  private final PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PadPipelineService padPipelineService;
  private final PipelineNumberAndSplitsService pipelineNumberAndSplitsService;
  private final EntityManager entityManager;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadOrganisationRoleService(
      PadOrganisationRolesRepository padOrganisationRolesRepository,
      PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PadPipelineService padPipelineService,
      PipelineNumberAndSplitsService pipelineNumberAndSplitsService,
      EntityManager entityManager,
      EntityCopyingService entityCopyingService) {
    this.padOrganisationRolesRepository = padOrganisationRolesRepository;
    this.padPipelineOrganisationRoleLinkRepository = padPipelineOrganisationRoleLinkRepository;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.padPipelineService = padPipelineService;
    this.pipelineNumberAndSplitsService = pipelineNumberAndSplitsService;
    this.entityManager = entityManager;
    this.entityCopyingService = entityCopyingService;
  }

  public List<PadOrganisationRole> getOrgRolesForDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padOrganisationRolesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  public List<PadOrganisationRole> getOrgRolesForDetailByRole(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole) {
    // performance is probably fine due to the relatively small numbers of roles expected per application on average
    return padOrganisationRolesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .filter(por -> por.getRole().equals(huooRole))
        .collect(toList());
  }

  public boolean hasOrganisationUnitRoleOwnersInRole(PwaApplicationDetail pwaApplicationDetail, HuooRole huooRole) {
    return padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        pwaApplicationDetail, huooRole, HuooType.PORTAL_ORG) > 0;
  }

  public boolean hasTreatyRoleOwnersInRole(PwaApplicationDetail pwaApplicationDetail, HuooRole huooRole) {
    return padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        pwaApplicationDetail, huooRole, HuooType.TREATY_AGREEMENT) > 0;
  }


  public PadOrganisationRole getOrganisationRole(PwaApplicationDetail pwaApplicationDetail, Integer id) {
    return padOrganisationRolesRepository.getByPwaApplicationDetailAndId(pwaApplicationDetail, id)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to find org role with ID: " + id));
  }

  public Set<OrganisationRoleInstanceDto> getOrganisationRoleDtos(PwaApplicationDetail pwaApplicationDetail) {
    return new HashSet<>(
        padOrganisationRolesRepository.findOrganisationRoleDtoByPwaApplicationDetail(pwaApplicationDetail)
    );

  }

  public Set<OrganisationRoleInstanceDto> getOrganisationRoleInstanceDtosByRole(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole) {
    return getOrganisationRoleDtos(pwaApplicationDetail).stream()
        .filter(o -> huooRole.equals(o.getHuooRole()))
        .collect(Collectors.toSet());

  }

  public List<HuooOrganisationUnitRoleView> getHuooOrganisationUnitRoleViews(PwaApplicationDetail detail,
                                                                             List<PadOrganisationRole> padOrganisationRoleList) {

    // filter so we are only looking at portal organisation roles
    Map<PortalOrganisationUnit, List<PadOrganisationRole>> orgRoles = padOrganisationRoleList.stream()
        .filter(orgRole -> orgRole.getType().equals(HuooType.PORTAL_ORG))
        .collect(Collectors.groupingBy(PadOrganisationRole::getOrganisationUnit));

    // get the org units so that we can query the details for each
    var portalOrgUnits = new ArrayList<>(orgRoles.keySet());

    Map<Integer, PortalOrganisationUnitDetail> portalOrgUnitDetails = portalOrganisationsAccessor
        .getOrganisationUnitDetails(portalOrgUnits).stream()
        .collect(toMap(PortalOrganisationUnitDetail::getOuId, orgUnitDetail -> orgUnitDetail));

    return orgRoles.keySet()
        .stream()
        .map(orgUnit -> {

          PortalOrganisationUnitDetail orgUnitDetail = portalOrgUnitDetails.getOrDefault(
              orgUnit.getOuId(), null);

          boolean canRemoveOrg = canRemoveOrgRoleFromUnit(detail, orgUnit);

          var roles = orgRoles.get(orgUnit)
              .stream()
              .map(PadOrganisationRole::getRole)
              .collect(Collectors.toSet());

          return new HuooOrganisationUnitRoleView(
              orgUnitDetail,
              roles,
              getEditHuooUrl(detail, orgUnit),
              canRemoveOrg ? getRemoveHuooUrl(detail, orgUnit) : null);

        })
        .sorted()
        .collect(toList());

  }

  public OrganisationRolesSummaryDto getOrganisationRoleSummary(PwaApplicationDetail detail) {
    var allOrganisationPipelineRoles = padOrganisationRolesRepository.findActiveOrganisationPipelineRolesByPwaApplicationDetail(
        detail);
    return OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(allOrganisationPipelineRoles);
  }

  private String getEditHuooUrl(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {
    return ReverseRouter.route(on(AddHuooController.class)
        .renderEditOrgHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationUnit.getOuId(), null, null, null));
  }

  private String getRemoveHuooUrl(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {
    return ReverseRouter.route(on(AddHuooController.class)
        .postDeleteOrgHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationUnit.getOuId(), null, null, null, null, null));
  }

  private String getRemoveHuooUrl(PwaApplicationDetail detail, PadOrganisationRole organisationRole) {
    return ReverseRouter.route(on(AddHuooController.class)
        .postDeleteTreatyHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationRole.getId(), null, null, null, null, null));
  }

  public List<HuooTreatyAgreementView> getTreatyAgreementViews(PwaApplicationDetail detail,
                                                               List<PadOrganisationRole> padOrganisationRoleList) {
    return padOrganisationRoleList.stream()
        .filter(padOrganisationRole -> padOrganisationRole.getType().equals(HuooType.TREATY_AGREEMENT))
        .map(treatyRole -> new HuooTreatyAgreementView(
            treatyRole,
            getRemoveHuooUrl(detail, treatyRole)))
        .sorted(Comparator.comparing(HuooTreatyAgreementView::getRoles))
        .collect(toList());
  }

  /**
   * If the organisation being removed is a holder, return true if there is > 1 holder on the application, false otherwise.
   * If the organisation being removed isn't a holder, return true.
   */
  public boolean canRemoveOrgRoleFromUnit(PwaApplicationDetail detail, PortalOrganisationUnit orgUnit) {
    var units = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail, orgUnit);
    var roles = units.stream()
        .map(PadOrganisationRole::getRole)
        .collect(Collectors.toSet());

    var countMap = getRoleCountMap(detail);

    if (roles.contains(HuooRole.HOLDER)) {
      return countMap.get(HuooRole.HOLDER) > 1;
    }
    return true;
  }

  @Transactional
  public void removeRolesOfUnit(PwaApplicationDetail pwaApplicationDetail, PortalOrganisationUnit organisationUnit) {
    var roles = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(pwaApplicationDetail,
        organisationUnit);

    removePipelineLinksForOrgsWithRoles(pwaApplicationDetail, roles);

    padOrganisationRolesRepository.deleteAll(roles);
  }

  @Transactional
  public void removePipelineLinksForOrgsWithRoles(PwaApplicationDetail detail, Collection<PadOrganisationRole> roles) {
    List<PadPipelineOrganisationRoleLink> pipelineLinks =
        padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
            roles, detail).stream()
            .filter(roleLink -> roles.stream()
                .anyMatch(
                    padOrganisationRole -> padOrganisationRole.getRole().equals(roleLink.getPadOrgRole().getRole())))
            .collect(Collectors.toUnmodifiableList());

    padPipelineOrganisationRoleLinkRepository.deleteAll(pipelineLinks);
  }

  @Transactional
  public void removeRoleOfTreatyAgreement(PadOrganisationRole organisationRole) {
    padOrganisationRolesRepository.delete(organisationRole);
  }

  public void mapPortalOrgUnitRoleToForm(PwaApplicationDetail detail, PortalOrganisationUnit orgUnit, HuooForm form) {
    var roles = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail, orgUnit);
    var roleSet = roles.stream()
        .map(PadOrganisationRole::getRole)
        .collect(Collectors.toSet());

    var role = roles.stream()
        .findFirst()
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "No organisation unit roles found for org unit with ID: " + orgUnit.getOuId()));
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setHuooRoles(roleSet);
    form.setOrganisationUnitId(role.getOrganisationUnit().getOuId());
  }

  public void mapTreatyAgreementToForm(PwaApplicationDetail pwaApplicationDetail, PadOrganisationRole organisationRole,
                                       HuooForm form) {
    if (organisationRole.getAgreement() == null) {
      throw new ActionNotAllowedException(
          "Attempting to edit a non-treaty agreement org with ID: " + organisationRole.getId());
    }
    form.setHuooType(organisationRole.getType());
    form.setHuooRoles(Set.of(organisationRole.getRole()));
  }

  /**
   * Removes existing linked entries of the organisationUnit, and creates the entries from the form information.
   *
   * @param detail The application detail
   * @param form   A validated HuooForm.
   */
  @Transactional
  public void saveEntityUsingForm(PwaApplicationDetail detail, HuooForm form) {
    var rolesToSave = new ArrayList<PadOrganisationRole>();

    if (form.getHuooType().equals(HuooType.PORTAL_ORG)) {

      var orgUnit = portalOrganisationsAccessor.getOrganisationUnitById(form.getOrganisationUnitId())
          .orElseThrow(() -> new PwaEntityNotFoundException(
              "Unable to find organisation unit with ID: " + form.getOrganisationUnitId()));

      var currentRoles = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail,
          orgUnit);

      Set<HuooRole> rolesToAdd = form.getHuooRoles()
          .stream()
          .filter(huooRole -> currentRoles.stream()
              .noneMatch(padOrganisationRole -> padOrganisationRole.getRole().equals(huooRole)))
          .collect(Collectors.toUnmodifiableSet());

      List<PadOrganisationRole> organisationRolesToRemove = currentRoles.stream()
          .filter(padOrganisationRole -> !form.getHuooRoles().contains(padOrganisationRole.getRole()))
          .collect(Collectors.toUnmodifiableList());

      removePipelineLinksForOrgsWithRoles(detail, organisationRolesToRemove);

      padOrganisationRolesRepository.deleteAll(organisationRolesToRemove);

      rolesToAdd.forEach(huooRole -> {
        var padOrganisationRole = new PadOrganisationRole();
        padOrganisationRole.setAgreement(null);
        padOrganisationRole.setPwaApplicationDetail(detail);
        padOrganisationRole.setRole(huooRole);
        padOrganisationRole.setType(form.getHuooType());
        padOrganisationRole.setOrganisationUnit(orgUnit);
        rolesToSave.add(padOrganisationRole);
      });

    } else if (form.getHuooType().equals(HuooType.TREATY_AGREEMENT)) {
      var padOrganisationRole = new PadOrganisationRole();
      padOrganisationRole.setAgreement(TreatyAgreement.ANY_TREATY_COUNTRY);
      padOrganisationRole.setPwaApplicationDetail(detail);
      padOrganisationRole.setOrganisationUnit(null);
      padOrganisationRole.setRole(HuooRole.USER);
      padOrganisationRole.setType(form.getHuooType());
      rolesToSave.add(padOrganisationRole);
    }
    padOrganisationRolesRepository.saveAll(rolesToSave);
  }

  @Transactional
  public void updateEntityUsingForm(PadOrganisationRole organisationRole, HuooForm form) {
    organisationRole.setAgreement(TreatyAgreement.ANY_TREATY_COUNTRY);
    padOrganisationRolesRepository.save(organisationRole);
  }

  @Transactional
  public void addHolder(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {

    var holderRole = new PadOrganisationRole();
    holderRole.setPwaApplicationDetail(detail);
    holderRole.setType(HuooType.PORTAL_ORG);
    holderRole.setRole(HuooRole.HOLDER);
    holderRole.setOrganisationUnit(organisationUnit);
    padOrganisationRolesRepository.save(holderRole);

  }

  public HuooValidationView getValidationViewForOrg(PwaApplicationDetail pwaApplicationDetail,
                                                    PortalOrganisationUnit portalOrganisationUnit) {
    var roles = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(pwaApplicationDetail,
        portalOrganisationUnit);
    return new HuooValidationView(new HashSet<>(roles));
  }

  public HuooValidationView getValidationViewForTreaty(PwaApplicationDetail pwaApplicationDetail,
                                                       PadOrganisationRole padOrganisationRole) {
    return new HuooValidationView(Set.of(padOrganisationRole));
  }

  /**
   * Return a count of all organisation roles currently on the application.
   *
   * @param pwaApplicationDetail The application detail.
   * @return A map with the role as key, and count as value.
   */
  @VisibleForTesting
  public Map<HuooRole, Integer> getRoleCountMap(PwaApplicationDetail pwaApplicationDetail) {
    var padOrganisationRoleList = getOrgRolesForDetail(pwaApplicationDetail);

    var map = new HashMap<HuooRole, Integer>();
    HuooRole.stream()
        .forEach(role -> map.put(role, 0));

    padOrganisationRoleList.stream()
        .map(PadOrganisationRole::getRole)
        .forEach(role -> map.put(role, map.get(role) + 1));

    return map;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var roleCountMap = getRoleCountMap(detail);
    return roleCountMap.get(HuooRole.HOLDER) > 0
        && roleCountMap.get(HuooRole.USER) > 0
        && roleCountMap.get(HuooRole.OPERATOR) > 0
        && roleCountMap.get(HuooRole.OWNER) > 0;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    if (validationType == ValidationType.FULL) {
      var roleCountMap = getRoleCountMap(pwaApplicationDetail);
      if (roleCountMap.get(HuooRole.HOLDER) == 0) {
        bindingResult.reject("holders" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "At least one holder is required");
      }
      if (roleCountMap.get(HuooRole.USER) == 0) {
        bindingResult.reject("users" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "At least one user is required");
      }
      if (roleCountMap.get(HuooRole.OPERATOR) == 0) {
        bindingResult.reject("operators" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "At least one operator is required");
      }
      if (roleCountMap.get(HuooRole.OWNER) == 0) {
        bindingResult.reject("owners" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "At least one owner is required");
      }
    }
    return bindingResult;
  }

  /* Given a summary of organisation roles and associated pipelines, create application entities */
  @Transactional
  public void createApplicationOrganisationRolesFromSummary(PwaApplicationDetail pwaApplicationDetail,
                                                            OrganisationRolesSummaryDto organisationRolesSummaryDto) {

    var padOrgRoles = createPadOrganisationRoleForEveryOrganisationRoleGroup(
        organisationRolesSummaryDto,
        pwaApplicationDetail);

    var persistedPadOrgRoleIterable = IterableUtils.toList(padOrganisationRolesRepository.saveAll(padOrgRoles));
    List<PadPipelineOrganisationRoleLink> padPipelineOrgRoleLinks = new ArrayList<>();

    Map<PipelineIdentifier, Pipeline> pipelineLookup = new HashMap<>();

    for (PadOrganisationRole padOrganisationRole : persistedPadOrgRoleIterable) {
      var orgRolePipelineGroup = organisationRolesSummaryDto.getOrganisationRolePipelineGroupBy(
          padOrganisationRole.getRole(),
          OrganisationUnitId.from(padOrganisationRole.getOrganisationUnit())
      );

      orgRolePipelineGroup.ifPresent(orgRoleGroup -> {
        orgRoleGroup.getPipelineIdentifiers().forEach(pipelineIdentifier -> {

          // create pipeline reference only when we dont have one in the same session.
          // at this point should we just get the pipeline object itself? cost is extra db hits.
          var pipeline = pipelineLookup.getOrDefault(pipelineIdentifier,
              //TODO PWA-676 need to handle pipeline splits
              entityManager.getReference(Pipeline.class, pipelineIdentifier.getPipelineIdAsInt()));
          pipelineLookup.putIfAbsent(pipelineIdentifier, pipeline);
          padPipelineOrgRoleLinks.add(
              new PadPipelineOrganisationRoleLink(padOrganisationRole, pipeline)
          );
        });
      });


    }

    padPipelineOrganisationRoleLinkRepository.saveAll(padPipelineOrgRoleLinks);

  }

  @Transactional
  public PadPipelineOrganisationRoleLink createPadPipelineOrganisationRoleLink(PadOrganisationRole padOrganisationRole,
                                                                               PipelineIdentifier pipelineIdentifier) {
    //  Not keen on this use of the visitor pattern. Try to rework if time, this involves a new pipeline object with the same id rather than
    // an actual Pipeline entity or reference.
    // to set on the new role, when all we have is the interface.
    var newRoleLink = new PadPipelineOrganisationRoleLink();
    newRoleLink.setPadOrgRole(padOrganisationRole);
    pipelineIdentifier.accept(newRoleLink);
    return padPipelineOrganisationRoleLinkRepository.save(newRoleLink);
  }

  @Transactional
  public void deletePadPipelineRoleLinksForPipelineIdentifiersAndRole(PwaApplicationDetail pwaApplicationDetail,
                                                                      Set<PipelineIdentifier> pipelineIdentifiers,
                                                                      HuooRole huooRole) {
    // distinct pipeline Ids, with split pipelines treated as whole.
    var pipelineIds = pipelineIdentifiers.stream()
        .map(PipelineIdentifier::getPipelineIdAsInt)
        .collect(toSet());

    // Performance note, this loads in a lot of data that we dont touch e.g org info.
    // We only need the role instance id and pipelineIdentifier data, could then use a simple reference to role for deletion.
    // possible that might remove requirement for the flush as linked objects wont be in the hibernate cache.
    var allRoleLinksForPipelines = padPipelineOrganisationRoleLinkRepository
        .findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
            pwaApplicationDetail,
            huooRole,
            pipelineIds
        ).stream()
        // filter out splits that have not been selected so they are not affected by delete
        .filter(r -> pipelineIdentifiers.contains(r.getPipelineIdentifier()))
        .collect(toList());

    padPipelineOrganisationRoleLinkRepository.deleteAll(allRoleLinksForPipelines);
    // this flush is required to make sure we force the transaction to send the DELETE to the database now
    entityManager.flush();
  }

  @Transactional
  public void deletePipelineRoleLinksForPadPipeline(PadPipeline padPipeline) {
    var links = padPipelineOrganisationRoleLinkRepository.getAllByPadOrgRole_PwaApplicationDetailAndPipeline(
        padPipeline.getPwaApplicationDetail(),
        padPipeline.getPipeline()
    );
    padPipelineOrganisationRoleLinkRepository.deleteAll(links);
  }

  public Set<PipelineIdentifier> getPipelineSplitsForRole(PwaApplicationDetail pwaApplicationDetail,
                                                          HuooRole huooRole) {
    return padPipelineOrganisationRoleLinkRepository.findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_Role(
        pwaApplicationDetail, huooRole).stream()
        // This could use a custom visitor ie PipelineIdentifierIsSplitVisitor which set a boolean flag to avoid this
        // class tagging but I dont think in practice that is much better.
        .filter(r -> r.getOrgRoleInstanceType().equals(OrgRoleInstanceType.SPLIT_PIPELINE))
        .map(PadPipelineOrganisationRoleLink::getPipelineIdentifier)
        .collect(toSet());
  }

  public boolean canShowHolderGuidance(PwaApplicationDetail pwaApplicationDetail) {
    return pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.INITIAL);
  }

  private List<PadOrganisationRole> createPadOrganisationRoleForEveryOrganisationRoleGroup(
      OrganisationRolesSummaryDto organisationRolesSummaryDto,
      PwaApplicationDetail pwaApplicationDetail) {

    var allOrganisationUnitIdWithRoleList = organisationRolesSummaryDto.getAllOrganisationUnitIdsWithRole();
    // just get org units once for easy lookup.
    Map<OrganisationUnitId, PortalOrganisationUnit> ouIdLookup = portalOrganisationsAccessor.getOrganisationUnitsByOrganisationUnitIdIn(
        allOrganisationUnitIdWithRoleList)
        .stream()
        .collect(toMap(OrganisationUnitId::from, ou -> ou));

    List<PadOrganisationRole> newPadOrgRoleList = new ArrayList<>();

    // might be a nicer/shorter way to do this eventually e.g get all org group dtos as list from summary object. For now it will do.
    newPadOrgRoleList.addAll(createPadOrganisationRoleFromGroupDtos(
        organisationRolesSummaryDto.getHolderOrganisationUnitGroups(),
        pwaApplicationDetail,
        ouIdLookup
    ));

    newPadOrgRoleList.addAll(createPadOrganisationRoleFromGroupDtos(
        organisationRolesSummaryDto.getUserOrganisationUnitGroups(),
        pwaApplicationDetail,
        ouIdLookup
    ));

    newPadOrgRoleList.addAll(createPadOrganisationRoleFromGroupDtos(
        organisationRolesSummaryDto.getOperatorOrganisationUnitGroups(),
        pwaApplicationDetail,
        ouIdLookup
    ));

    newPadOrgRoleList.addAll(createPadOrganisationRoleFromGroupDtos(
        organisationRolesSummaryDto.getOwnerOrganisationUnitGroups(),
        pwaApplicationDetail,
        ouIdLookup
    ));

    return newPadOrgRoleList;
  }

  private List<PadOrganisationRole> createPadOrganisationRoleFromGroupDtos(
      Collection<OrganisationRolePipelineGroupDto> organisationRolePipelineGroupDtos,
      PwaApplicationDetail pwaApplicationDetail,
      Map<OrganisationUnitId, PortalOrganisationUnit> orgUnitLookup) {
    List<PadOrganisationRole> newPadOrgRoleList = new ArrayList<>();
    organisationRolePipelineGroupDtos.forEach(o -> newPadOrgRoleList.add(PadOrganisationRole.fromOrganisationUnit(
        pwaApplicationDetail,
        orgUnitLookup.get(o.getOrganisationUnitId()),
        o.getHuooRole()
    )));
    return newPadOrgRoleList;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

    var padOrgRoleCopiedEntityIds = entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padOrganisationRolesRepository.getAllByPwaApplicationDetail(fromDetail),
         toDetail,
        PadOrganisationRole.class
    );

    var pipelineOrgRolesCopiedEntityIds = entityCopyingService.duplicateEntitiesAndSetParentFromCopiedEntities(
        () -> padPipelineOrganisationRoleLinkRepository.getAllByPadOrgRole_PwaApplicationDetail(fromDetail),
        padOrgRoleCopiedEntityIds,
        PadPipelineOrganisationRoleLink.class
    );


  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return !pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION);
  }

  @Transactional
  public void removalPipelineOrgRoleLinks(
      Collection<PadPipelineOrganisationRoleLink> padPipelineOrganisationRoleLinks) {
    padPipelineOrganisationRoleLinkRepository.deleteAll(padPipelineOrganisationRoleLinks);
  }

  @Transactional
  public void removeOrgRole(PadOrganisationRole padOrganisationRole) {
    padOrganisationRolesRepository.delete(padOrganisationRole);
  }

  public List<PadPipelineOrganisationRoleLink> getPipelineOrgRoleLinks(PwaApplicationDetail pwaApplicationDetail,
                                                                       HuooRole huooRole,
                                                                       PipelineId pipelineId) {
    return padPipelineOrganisationRoleLinkRepository.findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
        pwaApplicationDetail, huooRole, Set.of(pipelineId.asInt())
    );
  }

  private List<OrganisationRolePipelineGroupView> getOrgRolePipelineGroupView(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Map<OrganisationUnitId, OrganisationUnitDetailDto> orgUnitDetailsAndIdsMap,
      Set<OrganisationRolePipelineGroupDto> preComputedOrgRolePipelineGroups) {

    var allPipelineSplitInfoForRole = pipelineNumberAndSplitsService.getAllPipelineNumbersAndSplitsRole(
        () -> padPipelineService.getAllPipelineOverviewsFromAppAndMasterPwa(pwaApplicationDetail),
        () -> getPipelineSplitsForRole(pwaApplicationDetail, huooRole)
    );


    var views = new ArrayList<OrganisationRolePipelineGroupView>();
    preComputedOrgRolePipelineGroups.forEach(orgRolePipelineGroup -> {
      var numbersAndSplits = orgRolePipelineGroup.getPipelineIdentifiers().stream()
          .map(pipelineIdentifier -> allPipelineSplitInfoForRole.get(pipelineIdentifier))
          .collect(toList());

      var orgRolePipelinegroupView = new OrganisationRolePipelineGroupView(
          orgRolePipelineGroup.getHuooType(),
          orgUnitDetailsAndIdsMap.get(orgRolePipelineGroup.getOrganisationUnitId()),
          orgRolePipelineGroup.getHuooType().equals(HuooType.PORTAL_ORG)
              && orgUnitDetailsAndIdsMap.get(orgRolePipelineGroup.getOrganisationUnitId()) == null,
          orgRolePipelineGroup.getOrganisationRoleInstanceDto().getManualOrganisationName().orElse(null),
          orgRolePipelineGroup.getOrganisationRoleInstanceDto().getOrganisationRoleOwnerDto().getTreatyAgreement(),
          orgRolePipelineGroup.getOrganisationRoleInstanceDto().getOrganisationRoleOwnerDto(),
          numbersAndSplits);

      views.add(orgRolePipelinegroupView);
    });

    return views;
  }

  private Map<OrganisationUnitId, OrganisationUnitDetailDto> getOrgUnitDetailsAndIdsMap(
      OrganisationRolesSummaryDto organisationRolesSummaryDto) {
    Set<OrganisationRolePipelineGroupDto> allOrgUnitGroups = new HashSet<>();
    allOrgUnitGroups.addAll(organisationRolesSummaryDto.getHolderOrganisationUnitGroups());
    allOrgUnitGroups.addAll(organisationRolesSummaryDto.getUserOrganisationUnitGroups());
    allOrgUnitGroups.addAll(organisationRolesSummaryDto.getOperatorOrganisationUnitGroups());
    allOrgUnitGroups.addAll(organisationRolesSummaryDto.getOwnerOrganisationUnitGroups());

    Set<OrganisationUnitId> orgUnitIds = allOrgUnitGroups.stream()
        .map(OrganisationRolePipelineGroupDto::getOrganisationUnitId)
        .collect(Collectors.toSet());
    return portalOrganisationsAccessor.getOrganisationUnitDetailDtosByOrganisationUnitId(orgUnitIds).stream()
        .collect(toMap(OrganisationUnitDetailDto::getOrganisationUnitId, Function.identity()));
  }

  public AllOrgRolePipelineGroupsView getAllOrganisationRolePipelineGroupView(PwaApplicationDetail pwaApplicationDetail) {

    var orgRolesSummaryDto = getOrganisationRoleSummary(pwaApplicationDetail);

    Comparator<OrganisationRolePipelineGroupView> viewComparator =
        Comparator.comparing(OrganisationRolePipelineGroupView::getCompanyName,  Comparator.nullsLast(Comparator.naturalOrder()));

    Map<OrganisationUnitId, OrganisationUnitDetailDto> orgUnitDetailsAndIdsMap = getOrgUnitDetailsAndIdsMap(orgRolesSummaryDto);


    Set<OrganisationRolePipelineGroupDto> holderOrgUnitGroups = new HashSet<>();
    holderOrgUnitGroups.addAll(orgRolesSummaryDto.getHolderOrganisationUnitGroups());
    holderOrgUnitGroups.addAll(orgRolesSummaryDto.getHolderNonPortalOrgRoleGroups());
    List<OrganisationRolePipelineGroupView> holderOrgRolePipelineGroups = getOrgRolePipelineGroupView(
        pwaApplicationDetail, HuooRole.HOLDER,
        orgUnitDetailsAndIdsMap,
        holderOrgUnitGroups
    );
    holderOrgRolePipelineGroups.sort(viewComparator);

    Set<OrganisationRolePipelineGroupDto> userOrgUnitGroups = new HashSet<>();
    userOrgUnitGroups.addAll(orgRolesSummaryDto.getUserOrganisationUnitGroups());
    userOrgUnitGroups.addAll(orgRolesSummaryDto.getUserNonPortalOrgRoleGroups());
    List<OrganisationRolePipelineGroupView> userOrgRolePipelineGroups = getOrgRolePipelineGroupView(
        pwaApplicationDetail, HuooRole.USER,
        orgUnitDetailsAndIdsMap,
        userOrgUnitGroups
    );
    userOrgRolePipelineGroups.sort(viewComparator);

    Set<OrganisationRolePipelineGroupDto> operatorOrgUnitGroups = new HashSet<>();
    operatorOrgUnitGroups.addAll(orgRolesSummaryDto.getOperatorOrganisationUnitGroups());
    operatorOrgUnitGroups.addAll(orgRolesSummaryDto.getOperatorNonPortalOrgRoleGroups());
    List<OrganisationRolePipelineGroupView> operatorOrgRolePipelineGroups = getOrgRolePipelineGroupView(
        pwaApplicationDetail, HuooRole.OPERATOR,
        orgUnitDetailsAndIdsMap,
        operatorOrgUnitGroups
    );
    operatorOrgRolePipelineGroups.sort(viewComparator);

    Set<OrganisationRolePipelineGroupDto> ownerOrgUnitGroups = new HashSet<>();
    ownerOrgUnitGroups.addAll(orgRolesSummaryDto.getOwnerOrganisationUnitGroups());
    ownerOrgUnitGroups.addAll(orgRolesSummaryDto.getOwnerNonPortalOrgRoleGroups());
    List<OrganisationRolePipelineGroupView> ownerOrgRolePipelineGroups = getOrgRolePipelineGroupView(
        pwaApplicationDetail, HuooRole.OWNER,
        orgUnitDetailsAndIdsMap,
        ownerOrgUnitGroups
    );
    ownerOrgRolePipelineGroups.sort(viewComparator);

    return new AllOrgRolePipelineGroupsView(
        holderOrgRolePipelineGroups,
        userOrgRolePipelineGroups,
        operatorOrgRolePipelineGroups,
        ownerOrgRolePipelineGroups
    );
  }

}
