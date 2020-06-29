package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;

@Service
public class PipelineAndOrgRoleGroupViewFactory {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PadPipelineService padPipelineService;

  @Autowired
  public PipelineAndOrgRoleGroupViewFactory(
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PadPipelineService padPipelineService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.padPipelineService = padPipelineService;
  }


  public PipelineAndOrgRoleGroupViewsByRole createPipelineAndOrgsGroupsByRoleView(
      PwaApplicationDetail pwaApplicationDetail,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    Map<PipelineId, String> pipelineNumbers = padPipelineService.getApplicationOrConsentedPipelineNumberLookup(
        pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto.getAllPipelineIdsInSummary());

    Map<OrganisationUnitId, OrganisationUnitDetailDto> organisationUnitDetailDtos = portalOrganisationsAccessor
        .getOrganisationUnitDetailDtosByOrganisationUnitId(
            pipelineAndOrganisationRoleGroupSummaryDto.getAllOrganisationUnitIdsInSummary()
        )
        .stream()
        .collect(Collectors.toMap(OrganisationUnitDetailDto::getOrganisationUnitId, od -> od));

    Map<HuooRole, List<PipelinesAndOrgRoleGroupView>> pipelineAndOrgGroupViewsMappedByRole = createPipelineAndOrgGroupsMappedByRole(
        pipelineNumbers,
        organisationUnitDetailDtos,
        pipelineAndOrganisationRoleGroupSummaryDto
    );

    return new PipelineAndOrgRoleGroupViewsByRole(
        pipelineAndOrgGroupViewsMappedByRole.get(HuooRole.HOLDER),
        pipelineAndOrgGroupViewsMappedByRole.get(HuooRole.USER),
        pipelineAndOrgGroupViewsMappedByRole.get(HuooRole.OPERATOR),
        pipelineAndOrgGroupViewsMappedByRole.get(HuooRole.OWNER)
    );

  }

  private Map<HuooRole, List<PipelinesAndOrgRoleGroupView>> createPipelineAndOrgGroupsMappedByRole(
      Map<PipelineId, String> pipelineNumberLookup,
      Map<OrganisationUnitId, OrganisationUnitDetailDto> organisationUnitDetailDtoLookup,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    Map<HuooRole, List<PipelinesAndOrgRoleGroupView>> roleGroupsMappedByRole = getInitialisedRoleGroupMap();

    for (HuooRole role : HuooRole.values()) {
      for (PipelineAndOrganisationRoleGroupDto roleGroup : pipelineAndOrganisationRoleGroupSummaryDto.getGroupsByHuooRole(
          role)) {
        var pipelineNumberList = roleGroup.getPipelineIdSet().stream()
            .map(pipelineNumberLookup::get)
            .sorted(Comparator.comparing(String::toLowerCase))
            .collect(Collectors.toList());

        var orgNameList = roleGroup.getOrganisationRoleDtoSet().stream()
            .map(o -> organisationUnitDetailDtoLookup.get(o.getOrganisationUnitId()))
            .map(o -> o.getCompanyName() +
                (o.getRegisteredNumber() != null ? String.format(" (%s)", o.getRegisteredNumber()) : ""))
            .sorted(Comparator.comparing(String::toLowerCase))
            .collect(Collectors.toList());

        var roleGroupView = new PipelinesAndOrgRoleGroupView(
            role,
            roleGroup.getPipelineIdSet(),
            roleGroup.getOrganisationRoleDtoSet().stream()
                .map(OrganisationRoleDto::getOrganisationUnitId).collect(Collectors.toSet()),
            pipelineNumberList,
            orgNameList
        );

        roleGroupsMappedByRole.get(role).add(roleGroupView);
      }

    }

    return roleGroupsMappedByRole;

  }

  private Map<HuooRole, List<PipelinesAndOrgRoleGroupView>> getInitialisedRoleGroupMap() {
    return HuooRole.stream()
        .collect(Collectors.toMap(role -> role, role -> new ArrayList<>()));
  }


}
