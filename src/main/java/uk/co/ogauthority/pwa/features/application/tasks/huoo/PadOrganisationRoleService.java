package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.OrganisationRolePipelineGroupDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.OrganisationRolesSummaryDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrgRoleInstanceType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.aggregates.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.aggregates.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.model.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.generalcase.pipelinehuooview.PipelineNumberAndSplitsService;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

/**
 * Holds the core business logic around application based huoo operations.
 */
@Service
public class PadOrganisationRoleService {

  private final PadOrganisationRolesRepository padOrganisationRolesRepository;
  private final PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PipelineAndIdentViewFactory pipelineAndIdentViewFactory;
  private final PipelineNumberAndSplitsService pipelineNumberAndSplitsService;
  private final PadPipelineService padPipelineService;
  private final EntityManager entityManager;

  private final PadHuooRoleMetadataProvider padHuooRoleMetadataProvider;

  private final PwaApplicationService pwaApplicationService;

  @Autowired
  public PadOrganisationRoleService(
      PadOrganisationRolesRepository padOrganisationRolesRepository,
      PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PipelineAndIdentViewFactory pipelineAndIdentViewFactory,
      PipelineNumberAndSplitsService pipelineNumberAndSplitsService,
      PadPipelineService padPipelineService,
      EntityManager entityManager,
      PadHuooRoleMetadataProvider padHuooRoleMetadataProvider,
      PwaApplicationService pwaApplicationService) {
    this.padOrganisationRolesRepository = padOrganisationRolesRepository;
    this.padPipelineOrganisationRoleLinkRepository = padPipelineOrganisationRoleLinkRepository;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pipelineAndIdentViewFactory = pipelineAndIdentViewFactory;
    this.pipelineNumberAndSplitsService = pipelineNumberAndSplitsService;
    this.padPipelineService = padPipelineService;
    this.entityManager = entityManager;
    this.padHuooRoleMetadataProvider = padHuooRoleMetadataProvider;
    this.pwaApplicationService = pwaApplicationService;
  }

  public List<PadOrganisationRole> getOrgRolesForDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padOrganisationRolesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  public List<PadOrganisationRole> getAllAssignableAndNonAssignableOrgRolesForDetailByRole(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole) {
    // performance is probably fine due to the relatively small numbers of roles expected per application on average
    return padOrganisationRolesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .filter(por -> por.getRole().equals(huooRole))
        .collect(toList());
  }

  public List<PadOrganisationRole> getAssignableOrgRolesForDetailByRole(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole) {
    // performance is probably fine due to the relatively small numbers of roles expected per application on average
    return getAllAssignableAndNonAssignableOrgRolesForDetailByRole(pwaApplicationDetail, huooRole)
        .stream()
        .filter(por -> !por.getType().equals(HuooType.UNASSIGNED_PIPELINE_SPLIT))
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

  public Set<OrganisationRoleInstanceDto> getAssignableOrganisationRoleDtos(PwaApplicationDetail pwaApplicationDetail) {
    return  padOrganisationRolesRepository.findOrganisationRoleDtoByPwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .filter(o -> !o.getHuooType().equals(HuooType.UNASSIGNED_PIPELINE_SPLIT))
        .collect(toSet());

  }

  public Set<OrganisationRoleInstanceDto> getAssignableOrganisationRoleInstanceDtosByRole(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole) {
    return getAssignableOrganisationRoleDtos(pwaApplicationDetail).stream()
        .filter(o -> huooRole.equals(o.getHuooRole()))
        .collect(Collectors.toSet());

  }

  private OrganisationRolesSummaryDto getOrganisationRoleSummary(PwaApplicationDetail detail) {

    Set<PipelineId> activeAppPipelineIds = new HashSet<>();
    Set<PipelineId> inactiveAppPipelineIds = new HashSet<>();

    // Get active and inactive pipeline ids for pad pipelines on the app detail
    var pipelineInactiveStatuses = padPipelineService.getPadPipelineInactiveStatuses();
    padPipelineService.getPipelines(detail).forEach(padPipeline -> {
      if (pipelineInactiveStatuses.contains(padPipeline.getPipelineStatus())) {
        inactiveAppPipelineIds.add(padPipeline.getPipelineId());
      } else {
        activeAppPipelineIds.add(padPipeline.getPipelineId());
      }
    });

    // Get active organisation pipeline roles as dtos
    // If org role is for an inactive pipeline, keep the role owner but discard pipeline by creating new role instance dto
    var allOrganisationPipelineRoles = padOrganisationRolesRepository.findActiveOrganisationPipelineRolesByPwaApplicationDetail(detail);
    var activeOrgPipelineRoles = allOrganisationPipelineRoles.stream()
        .map(orgPipelineRole -> {
          if (orgPipelineRole.getPipelineIdentifier() != null
              && inactiveAppPipelineIds.contains(orgPipelineRole.getPipelineIdentifier().getPipelineId())) {
            return OrganisationPipelineRoleInstanceDto.copyWithoutPipeline(orgPipelineRole);
          }
          return orgPipelineRole;
        })
        .collect(Collectors.toList());

    // group up pipeline ids from roles by H/U/O/O role type
    Map<HuooRole, Set<PipelineId>> huooRoleToPipelineIds = activeOrgPipelineRoles.stream()
        .filter(activeOrgPipelineRole -> activeOrgPipelineRole.getPipelineIdentifier() != null)
        .collect(groupingBy(
            OrganisationPipelineRoleInstanceDto::getHuooRole,
            Collectors.mapping(orgPipelineRole -> orgPipelineRole.getPipelineIdentifier().getPipelineId(), Collectors.toSet())
        ));

    // for each H/U/O/O role type
    for (HuooRole role : HuooRole.values()) {

      // if there is at least one pipeline assigned to the role type
      if (activeOrgPipelineRoles.stream().anyMatch(orgRole -> orgRole.getHuooRole().equals(role))) {

        // add any pipelines that have not yet been assigned a role of this type to the list
        var activePipelineIdsForRole = huooRoleToPipelineIds.getOrDefault(role, Set.of());
        var unassignedPipelinesForRole = SetUtils.difference(activeAppPipelineIds, activePipelineIdsForRole);
        unassignedPipelinesForRole.forEach(pipelineId -> activeOrgPipelineRoles.add(
            OrganisationPipelineRoleInstanceDto.manualPipelineRoleInstance(
                pipelineId, role, String.format("Pipelines without assigned %s", role.getDisplayText()))));
      }
    }


    return OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(activeOrgPipelineRoles);
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

    var countMap = padHuooRoleMetadataProvider.getRoleCountMap(detail);

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

  @VisibleForTesting
  void removePipelineLinksForOrgsWithRoles(PwaApplicationDetail detail, Collection<PadOrganisationRole> roles) {

    List<PadPipelineOrganisationRoleLink> pipelineLinks =
        padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
            roles, detail).stream()
            .filter(roleLink -> roles.stream()
                .anyMatch(
                    padOrganisationRole -> padOrganisationRole.getRole().equals(roleLink.getPadOrgRole().getRole())))
            .collect(Collectors.toUnmodifiableList());


    List<PadPipelineOrganisationRoleLink> pipelineLinksToRemove = new ArrayList<>();
    List<PadPipelineOrganisationRoleLink> pipelineLinksToUpdate = new ArrayList<>();

    //To avoid removing the last instance of a previously defined section of a split pipeline,
    //we need to find out if the specific section, identified by pipeline and section number, exists more than once.
    //Section number is a safe proxy for pipeline sections and avoids a filter on each attribute that makes up a split
    pipelineLinks.forEach(link -> {
      if (link.getOrgRoleInstanceType().equals(OrgRoleInstanceType.SPLIT_PIPELINE)) {
        var duplicateSectionLinks = padPipelineOrganisationRoleLinkRepository
            .countByPadOrgRole_PwaApplicationDetailAndPadOrgRole_RoleAndPipelineAndSectionNumber(
                detail, link.getPadOrgRole().getRole(), link.getPipeline(), link.getSectionNumber()
            );

        //If the section exists more than once, we can delete as there still exists some other assigned instance.
        //If the split section has only 1 instance, do not remove and instead assign it to the "Unassigned pipeline split" role so that ..
        //we keep all sections of a split pipeline in the data.
        if (duplicateSectionLinks > 1) {
          pipelineLinksToRemove.add(link);

        } else {
          pipelineLinksToUpdate.add(link);
        }

      } else {
        pipelineLinksToRemove.add(link);
      }
    });

    for (PadPipelineOrganisationRoleLink pipelineLink : pipelineLinksToUpdate) {
      var tempRoleForPipelineSplits = getOrCreateUnassignedPipelineSplitRole(detail, pipelineLink.getPadOrgRole().getRole());
      pipelineLink.setPadOrgRole(tempRoleForPipelineSplits);
    }
    padPipelineOrganisationRoleLinkRepository.saveAll(pipelineLinksToUpdate);
    padPipelineOrganisationRoleLinkRepository.deleteAll(pipelineLinksToRemove);
  }

  @Transactional
  public void removeRoleOfTreatyAgreement(PadOrganisationRole organisationRole) {
    removePipelineLinksForOrgsWithRoles(organisationRole.getPwaApplicationDetail(), List.of(organisationRole));
    padOrganisationRolesRepository.delete(organisationRole);
  }

  public void removePipelineLinksForRetiredPipelines(List<Pipeline> retiredPipelines) {
    var pipelineLinksToRemove = padPipelineOrganisationRoleLinkRepository.findAllDraftLinksForRetiredPipelines(
        PwaApplicationStatus.updatableStatuses(),
        retiredPipelines
    );
    padPipelineOrganisationRoleLinkRepository.deleteAll(pipelineLinksToRemove);
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


  @Transactional
  public void updateOrgRolesUsingForm(PwaApplicationDetail detail, HuooForm form, PortalOrganisationUnit existingOrgUnit) {

    var orgUnitToAdd = portalOrganisationsAccessor.getOrganisationUnitById(form.getOrganisationUnitId())
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Unable to find organisation unit with ID: " + form.getOrganisationUnitId()));

    List<PadOrganisationRole> existingOrgRoles = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail,
        existingOrgUnit);

    //get organisations that need to be saved on the application
    List<PadOrganisationRole> orgRolesToSave = new ArrayList<>();
    Set<HuooRole> newHuooRolesForOrg = form.getHuooRoles();
    Set<HuooRole> huooRolesNotGivenToOrg = EnumSet.complementOf(EnumSet.copyOf(newHuooRolesForOrg));

    newHuooRolesForOrg.forEach(huooRole -> {
      var orgToUpdateOpt = existingOrgRoles.stream()
          .filter(existingOrgRole -> huooRole.equals(existingOrgRole.getRole()))
          .findFirst();

      if (orgToUpdateOpt.isEmpty()) {
        var padOrganisationRole = PadOrganisationRole.fromOrganisationUnit(detail, orgUnitToAdd, huooRole);
        orgRolesToSave.add(padOrganisationRole);

      } else {
        var orgToUpdate = orgToUpdateOpt.get();
        orgToUpdate.setOrganisationUnit(orgUnitToAdd);
        orgRolesToSave.add(orgToUpdate);
      }
    });

    //save new orgs, remove old orgs, remove old pipeline links
    List<PadOrganisationRole> orgRolesToRemove = existingOrgRoles.stream()
        .filter(existingOrgRole -> huooRolesNotGivenToOrg.contains(existingOrgRole.getRole()))
        .collect(Collectors.toList());

    padOrganisationRolesRepository.saveAll(orgRolesToSave);
    removePipelineLinksForOrgsWithRoles(detail, orgRolesToRemove);
    padOrganisationRolesRepository.deleteAll(orgRolesToRemove);

    // if new PWA, update the applicant organisation to ensure it matches the holder for the application
    if (detail.getPwaApplicationType() == PwaApplicationType.INITIAL) {
      orgRolesToSave.stream()
          .filter(r -> r.getRole() == HuooRole.HOLDER)
          .findFirst()
          .ifPresent(h -> pwaApplicationService
              .updateApplicantOrganisationUnitId(detail.getPwaApplication(), h.getOrganisationUnit()));
    }

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
          .filter(padOrganisationRole -> !form.getHuooRoles().contains(padOrganisationRole.getRole())).collect(
              Collectors.toList());

      removePipelineLinksForOrgsWithRoles(detail, organisationRolesToRemove);


      if (form.getHuooRoles().contains(HuooRole.HOLDER)) {
        var existingHolderOrgs = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndRole(detail, HuooRole.HOLDER);
        if (!existingHolderOrgs.isEmpty() && !existingHolderOrgs.get(0).getOrganisationUnit().equals(orgUnit)) {
          organisationRolesToRemove.addAll(existingHolderOrgs);

        }
      }


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

  public HuooValidationView getValidationViewForTreaty(PadOrganisationRole padOrganisationRole) {
    return new HuooValidationView(Set.of(padOrganisationRole));
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
          var pipeline = pipelineLookup.getOrDefault(
              pipelineIdentifier,
              entityManager.getReference(Pipeline.class, pipelineIdentifier.getPipelineIdAsInt()));

          pipelineLookup.putIfAbsent(pipelineIdentifier, pipeline);

          var pipelineRoleLink = new PadPipelineOrganisationRoleLink(padOrganisationRole, pipeline);

          // use the role link visitor to set the correct information based on the implementation of PipelineIdentifier
          pipelineIdentifier.accept(pipelineRoleLink);

          padPipelineOrgRoleLinks.add(pipelineRoleLink);

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

  public Map<PipelineIdentifier, PipelineNumbersAndSplits> getAllPipelineNumbersAndSplitsForRole(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole
  ) {
    return pipelineNumberAndSplitsService.getAllPipelineNumbersAndSplitsRole(
        () -> pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
            pwaApplicationDetail,
            PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES),
        () -> getPipelineSplitsForRole(pwaApplicationDetail, huooRole)
    );
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

  @Transactional
  public void removalPipelineOrgRoleLinks(
      Collection<PadPipelineOrganisationRoleLink> padPipelineOrganisationRoleLinks) {
    padPipelineOrganisationRoleLinkRepository.deleteAll(padPipelineOrganisationRoleLinks);
  }

  @Transactional
  public void removeOrgRole(PadOrganisationRole padOrganisationRole) {
    padOrganisationRolesRepository.delete(padOrganisationRole);
  }

  @Transactional
  public PadOrganisationRole getOrCreateUnassignedPipelineSplitRole(PwaApplicationDetail pwaApplicationDetail,
                                                                     HuooRole huooRole) {
    var unassignedPipelineSplitRole =  getAllAssignableAndNonAssignableOrgRolesForDetailByRole(pwaApplicationDetail, huooRole)
        .stream()
        .filter(role -> HuooType.UNASSIGNED_PIPELINE_SPLIT.equals(role.getType()))
        .findFirst()
        .orElse(PadOrganisationRole.forUnassignedSplitPipeline(pwaApplicationDetail, huooRole));

    return padOrganisationRolesRepository.save(unassignedPipelineSplitRole);

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
        () -> pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
            pwaApplicationDetail,
            PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES
        ),
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

  /**
   * Returns an aggregate containing the HUOO associations for pipelines on the specific application detail.
   *
   * <p>If the pipeline, does not appear in the application, returns the PWA pipelines from the consented model that are still
   * on the seabed.</p>
   */
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

  public boolean organisationExistsAndActive(Integer ouId) {
    var orgUnit = portalOrganisationsAccessor.getOrganisationUnitById(ouId);
    return orgUnit.isPresent() && orgUnit.get().isActive();
  }

}
