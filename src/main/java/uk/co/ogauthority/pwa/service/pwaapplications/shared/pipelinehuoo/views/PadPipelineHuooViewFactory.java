package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
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


  private Map<OrganisationRoleOwnerDto, String> createOrganisationNameLookupFromOrgRoles(
      PwaApplicationDetail pwaApplicationDetail) {

    Set<OrganisationRoleOwnerDto> allOrgsWithRole = padOrganisationRoleService.getOrganisationRoleDtos(
        pwaApplicationDetail
    ).stream()
        .map(OrganisationRoleInstanceDto::getOrganisationRoleOwnerDto)
        .collect(Collectors.toSet());

    var organisationUnitIds = allOrgsWithRole.stream()
        .filter(o -> Objects.nonNull(o.getOrganisationUnitId()))
        .map(OrganisationRoleOwnerDto::getOrganisationUnitId)
        .collect(Collectors.toSet());

    Map<OrganisationUnitId, String> orgUnitNameLookup = portalOrganisationsAccessor.getOrganisationUnitDetailDtosByOrganisationUnitId(
        organisationUnitIds
    )
        .stream()
        .collect(Collectors.toMap(OrganisationUnitDetailDto::getOrganisationUnitId,
            this::createCompositeOrganisationUnitName));

    return allOrgsWithRole.stream()
        .collect(Collectors.toMap(o -> o, o -> {
          if (orgUnitNameLookup.containsKey(o.getOrganisationUnitId())) {
            return orgUnitNameLookup.get(o.getOrganisationUnitId());
          }
          return String.format("Country wide treaty (%s)", o.getTreatyAgreement().getCountry());
        }));

  }

  public PipelineAndOrgRoleGroupViewsByRole createPipelineAndOrgGroupViewsByRole(
      PwaApplicationDetail pwaApplicationDetail,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    Map<PipelineId, String> pipelineNumbers = padPipelineService.getApplicationOrConsentedPipelineNumberLookup(
        pwaApplicationDetail
    );

    Map<OrganisationRoleOwnerDto, String> organisationNameLookup = createOrganisationNameLookupFromOrgRoles(
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
  private Map<OrganisationRoleOwnerDto, String> getUnassignedOrganisationRoleOwnerNameLookupForRole(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Map<OrganisationRoleOwnerDto, String> organisationRoleOwnerNameLookup,
      Set<OrganisationRoleOwnerDto> organisationRoleOwnersWithRole) {

    Set<OrganisationRoleOwnerDto> allOrgRoleOwnersWithRole = padOrganisationRoleService.getOrganisationRoleInstanceDtosByRole(
        pwaApplicationDetail,
        huooRole
    ).stream()
        .map(OrganisationRoleInstanceDto::getOrganisationRoleOwnerDto)
        .collect(Collectors.toSet());

    // Minus organisations attached to a pipeline role from the complete list of organisations with the role on App
    return SetUtils.difference(allOrgRoleOwnersWithRole, organisationRoleOwnersWithRole).stream()
        .collect(Collectors.toMap(o -> o, organisationRoleOwnerNameLookup::get));
  }

  private PipelineHuooRoleSummaryView createPipelineHuooRoleSummaryView(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Map<PipelineId, String> pipelineNumberLookup,
      Map<OrganisationRoleOwnerDto, String> organisationRoleOwnerNameLookup,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    Map<PipelineId, String> unassignedPipelineIdsForRole = getUnassignedPipelineIdLookupForRole(
        pipelineNumberLookup,
        pipelineAndOrganisationRoleGroupSummaryDto.getPipelineIdsWithAssignedRole(huooRole)
    );

    Map<OrganisationRoleOwnerDto, String> unassignedOrganisationUnitsForRole = getUnassignedOrganisationRoleOwnerNameLookupForRole(
        pwaApplicationDetail,
        huooRole,
        organisationRoleOwnerNameLookup,
        pipelineAndOrganisationRoleGroupSummaryDto.getOrganisationRoleOwnersWithAssignedRole(huooRole)
    );

    return new PipelineHuooRoleSummaryView(
        huooRole,
        createPipelineAndOrgGroupsForRole(
            huooRole,
            pipelineNumberLookup,
            organisationRoleOwnerNameLookup,
            pipelineAndOrganisationRoleGroupSummaryDto
        ),
        unassignedPipelineIdsForRole,
        unassignedOrganisationUnitsForRole
    );
  }

  private List<PipelinesAndOrgRoleGroupView> createPipelineAndOrgGroupsForRole(
      HuooRole huooRole,
      Map<PipelineId, String> pipelineNumberLookup,
      Map<OrganisationRoleOwnerDto, String> organisationRoleOwnerNameLookup,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    List<PipelinesAndOrgRoleGroupView> roleGroupsMappedByRole = new ArrayList<>();

    for (PipelineAndOrganisationRoleGroupDto roleGroup : pipelineAndOrganisationRoleGroupSummaryDto.getGroupsByHuooRole(
        huooRole)) {
      var pipelineNumberList = roleGroup.getPipelineIdSet().stream()
          .map(pipelineNumberLookup::get)
          .sorted(Comparator.comparing(String::toLowerCase))
          .collect(Collectors.toList());

      var orgNameList = roleGroup.getOrganisationRoleOwnerDtoSet().stream()
          .map(organisationRoleOwnerNameLookup::get)
          .sorted(Comparator.comparing(String::toLowerCase))
          .collect(Collectors.toList());

      var roleGroupView = new PipelinesAndOrgRoleGroupView(

          roleGroup.getPipelineIdSet(),
          roleGroup.getOrganisationRoleOwnerDtoSet(),
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
