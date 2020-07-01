package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;

@Service
/**
 * Service which constructs view objects over application pipeline huoo data designed to be consumed in Templates
 */
public class PadPipelineHuooViewFactory {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PadPipelineService padPipelineService;
  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public PadPipelineHuooViewFactory(
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PadPipelineService padPipelineService,
      PadOrganisationRoleService padOrganisationRoleService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.padPipelineService = padPipelineService;
    this.padOrganisationRoleService = padOrganisationRoleService;
  }


  private Map<OrganisationUnitId, String> createOrganisationNameLookupFromOrgRoles(
      PwaApplicationDetail pwaApplicationDetail) {

    Set<OrganisationUnitId> allOrgsWithRole = padOrganisationRoleService.getOrganisationRoleDtos(
        pwaApplicationDetail
    ).stream()
        .filter(organisationRoleDto -> HuooType.PORTAL_ORG.equals(organisationRoleDto.getHuooType()))
        .map(OrganisationRoleInstanceDto::getOrganisationUnitId)
        .collect(Collectors.toSet());

    return portalOrganisationsAccessor.getOrganisationUnitDetailDtosByOrganisationUnitId(
        allOrgsWithRole
    )
        .stream()
        .collect(Collectors.toMap(OrganisationUnitDetailDto::getOrganisationUnitId,
            this::createCompositeOrganisationUnitName));

  }

  public PipelineAndOrgRoleGroupViewsByRole createPipelineAndOrgGroupViewsByRole(
      PwaApplicationDetail pwaApplicationDetail,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    Map<PipelineId, String> pipelineNumbers = padPipelineService.getApplicationOrConsentedPipelineNumberLookup(
        pwaApplicationDetail
    );

    Map<OrganisationUnitId, String> organisationNameLookup = createOrganisationNameLookupFromOrgRoles(
        pwaApplicationDetail);

    Map<HuooRole, PipelineHuooRoleSummaryView> pipelineHuooRoleSummaryByRole = new HashMap<>();

    for (HuooRole role : HuooRole.values()) {
      pipelineHuooRoleSummaryByRole.put(
          role,
          createPipelineHuooRoleSummaryView(
              pwaApplicationDetail,
              role,
              pipelineNumbers,
              organisationNameLookup,
              pipelineAndOrganisationRoleGroupSummaryDto
          )
      );

    }

    return new PipelineAndOrgRoleGroupViewsByRole(
        pipelineHuooRoleSummaryByRole.get(HuooRole.HOLDER),
        pipelineHuooRoleSummaryByRole.get(HuooRole.USER),
        pipelineHuooRoleSummaryByRole.get(HuooRole.OPERATOR),
        pipelineHuooRoleSummaryByRole.get(HuooRole.OWNER)
    );

  }


  /**
   * Helper to create name lookup of only pipelines without a given huoo role.
   */
  private Map<PipelineId, String> getUnassignedPipelineIdLookupForRole(Map<PipelineId, String> pipelineNumberLookup,
                                                                       Set<PipelineId> pipelineIdsWithOrgRole) {
    // Minus the pipelines attached to a role from the complete pipeline set for the application and PWA
    return SetUtils.difference(
        pipelineNumberLookup.keySet(),
        pipelineIdsWithOrgRole
    ).stream()
        .collect(Collectors.toMap(pid -> pid, pipelineNumberLookup::get));
  }

  /**
   * Helper to create a lookup of organisation id to name for orgs with no associated pipelines for given huoo role.
   */
  private Map<OrganisationUnitId, String> getUnassignedOrganisationUnitNameLookupForRole(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Map<OrganisationUnitId, String> organisationUnitNameLookup,
      Set<OrganisationUnitId> organisationUnitIdsWithRole) {

    Set<OrganisationUnitId> allOrgsWithRole = padOrganisationRoleService.getOrganisationRoleDtosByRole(
        pwaApplicationDetail,
        huooRole,
        HuooType.PORTAL_ORG
    ).stream()
        .map(OrganisationRoleInstanceDto::getOrganisationUnitId)
        .collect(Collectors.toSet());

    // Minus organisations attached to a pipeline role from the complete list of organisations with the role on App
    return SetUtils.difference(allOrgsWithRole, organisationUnitIdsWithRole).stream()
        .collect(Collectors.toMap(o -> o, organisationUnitNameLookup::get));
  }

  private PipelineHuooRoleSummaryView createPipelineHuooRoleSummaryView(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Map<PipelineId, String> pipelineNumberLookup,
      Map<OrganisationUnitId, String> organisationUnitNameLookup,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    Map<PipelineId, String> unassignedPipelineIdsForRole = getUnassignedPipelineIdLookupForRole(
        pipelineNumberLookup,
        pipelineAndOrganisationRoleGroupSummaryDto.getPipelineIdsWithAssignedRole(huooRole)
    );

    Map<OrganisationUnitId, String> unassignedOrganisationUnitsForRole = getUnassignedOrganisationUnitNameLookupForRole(
        pwaApplicationDetail,
        huooRole,
        organisationUnitNameLookup,
        pipelineAndOrganisationRoleGroupSummaryDto.getOrganisationUnitIdsWithAssignedRole(huooRole)
    );

    return new PipelineHuooRoleSummaryView(
        huooRole,
        createPipelineAndOrgGroupsForRole(
            huooRole,
            pipelineNumberLookup,
            organisationUnitNameLookup,
            pipelineAndOrganisationRoleGroupSummaryDto
        ),
        unassignedPipelineIdsForRole,
        unassignedOrganisationUnitsForRole
    );
  }

  private List<PipelinesAndOrgRoleGroupView> createPipelineAndOrgGroupsForRole(
      HuooRole huooRole,
      Map<PipelineId, String> pipelineNumberLookup,
      Map<OrganisationUnitId, String> organisationUnitNameLookup,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    List<PipelinesAndOrgRoleGroupView> roleGroupsMappedByRole = new ArrayList<>();

    for (PipelineAndOrganisationRoleGroupDto roleGroup : pipelineAndOrganisationRoleGroupSummaryDto.getGroupsByHuooRole(
        huooRole)) {
      var pipelineNumberList = roleGroup.getPipelineIdSet().stream()
          .map(pipelineNumberLookup::get)
          .sorted(Comparator.comparing(String::toLowerCase))
          .collect(Collectors.toList());

      var orgNameList = roleGroup.getOrganisationRoleInstanceDtoSet().stream()
          .map(o -> organisationUnitNameLookup.get(o.getOrganisationUnitId()))
          .sorted(Comparator.comparing(String::toLowerCase))
          .collect(Collectors.toList());

      var roleGroupView = new PipelinesAndOrgRoleGroupView(

          roleGroup.getPipelineIdSet(),
          roleGroup.getOrganisationRoleInstanceDtoSet().stream()
              .map(OrganisationRoleInstanceDto::getOrganisationUnitId).collect(Collectors.toSet()),
          pipelineNumberList,
          orgNameList
      );

      roleGroupsMappedByRole.add(roleGroupView);

    }

    return roleGroupsMappedByRole;

  }

  private String createCompositeOrganisationUnitName(OrganisationUnitDetailDto organisationUnitDetailDto) {
    return organisationUnitDetailDto.getCompanyName() +
        (organisationUnitDetailDto.getRegisteredNumber() != null ? String.format(" (%s)",
            organisationUnitDetailDto.getRegisteredNumber()) : "");
  }


}
