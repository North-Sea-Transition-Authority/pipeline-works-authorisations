package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;


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
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.PipelineAndOrganisationRoleGroupDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.model.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;

/**
 * Service which constructs view objects over application pipeline huoo data designed to be consumed in Templates.
 */
@Service
public class PadPipelineHuooViewFactory {

  private static final String DEFAULT_SPLIT_PIPELINE_DISPLAY_TEXT = "Unassigned";

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public PadPipelineHuooViewFactory(
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PadPipelineService padPipelineService,
      PadOrganisationRoleService padOrganisationRoleService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.padOrganisationRoleService = padOrganisationRoleService;
  }


  private Map<OrganisationRoleOwnerDto, String> createOrganisationNameLookupFromOrgRoles(
      PwaApplicationDetail pwaApplicationDetail) {

    Set<OrganisationRoleOwnerDto> allOrgsWithRole = padOrganisationRoleService.getAssignableOrganisationRoleDtos(
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
        .collect(
            Collectors.toMap(
                organisationRoleOwnerDto -> organisationRoleOwnerDto,
                organisationRoleOwnerDto -> getOrganisationRoleOwnerDisplayName(orgUnitNameLookup, organisationRoleOwnerDto)
            )
        );
  }

  private String getOrganisationRoleOwnerDisplayName(Map<OrganisationUnitId, String> orgUnitNameLookup,
                                                     OrganisationRoleOwnerDto organisationRoleOwnerDto) {
    switch (organisationRoleOwnerDto.getHuooType()) {
      case PORTAL_ORG:
        if (!orgUnitNameLookup.containsKey(organisationRoleOwnerDto.getOrganisationUnitId())) {
          throw new RuntimeException("Expected to find org unit with id:" + organisationRoleOwnerDto.getOrganisationUnitId().asInt());
        }
        return orgUnitNameLookup.get(organisationRoleOwnerDto.getOrganisationUnitId());

      case TREATY_AGREEMENT:
        return organisationRoleOwnerDto.getTreatyAgreement().getAgreementText();

      default:
        return DEFAULT_SPLIT_PIPELINE_DISPLAY_TEXT;
    }
  }

  public PipelineAndOrgRoleGroupViewsByRole createPipelineAndOrgGroupViewsByRole(
      PwaApplicationDetail pwaApplicationDetail,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    Map<OrganisationRoleOwnerDto, String> organisationNameLookup = createOrganisationNameLookupFromOrgRoles(
        pwaApplicationDetail);

    Map<HuooRole, PipelineHuooRoleSummaryView> pipelineHuooRoleSummaryByRole = new HashMap<>();

    for (HuooRole role : HuooRole.values()) {
      pipelineHuooRoleSummaryByRole.put(
          role,
          createPipelineHuooRoleSummaryView(
              pwaApplicationDetail,
              role,
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
  private Map<PipelineIdentifier, String> getUnassignedPipelineIdLookupForRole(
      Map<PipelineIdentifier, String> pipelineNumberLookup,
      Set<PipelineId> pipelineIdsWithOrgRole) {
    // Minus the pipelines attached to a role from the complete pipeline set for the application and PWA
    // only care about whole pipelines here, so use pipeline id only.
    var allPipelineIdsInLookup = pipelineNumberLookup.keySet()
        .stream()
        .map(PipelineIdentifier::getPipelineId)
        .collect(Collectors.toSet());

    return SetUtils.difference(
        allPipelineIdsInLookup,
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

    Set<OrganisationRoleOwnerDto> allOrgRoleOwnersWithRole = padOrganisationRoleService.getAssignableOrganisationRoleInstanceDtosByRole(
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
      Map<OrganisationRoleOwnerDto, String> organisationRoleOwnerNameLookup,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    var pipelineNumberLookupForRole = padOrganisationRoleService.getAllPipelineNumbersAndSplitsForRole(
        pwaApplicationDetail, huooRole);

    var unassignedPipelineIdentifiersForRole = pipelineNumberLookupForRole
        .entrySet()
        .stream()
        .filter(entry -> !pipelineAndOrganisationRoleGroupSummaryDto.getPipelineIdsWithAssignedRole(huooRole).contains(entry.getKey()))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            numbersAndSplitsEntry -> getPipelineDisplayName(numbersAndSplitsEntry.getValue()))
        );

    Map<OrganisationRoleOwnerDto, String> unassignedOrganisationRoleOwnersForRole = getUnassignedOrganisationRoleOwnerNameLookupForRole(
        pwaApplicationDetail,
        huooRole,
        organisationRoleOwnerNameLookup,
        pipelineAndOrganisationRoleGroupSummaryDto.getOrganisationRoleOwnersWithAssignedRole(huooRole)
    );

    return new PipelineHuooRoleSummaryView(
        huooRole,
        createPipelineAndOrgGroupsForRole(
            huooRole,
            pipelineNumberLookupForRole,
            organisationRoleOwnerNameLookup,
            pipelineAndOrganisationRoleGroupSummaryDto
        ),
        unassignedPipelineIdentifiersForRole,
        unassignedOrganisationRoleOwnersForRole
    );
  }

  private String getPipelineDisplayName(PipelineNumbersAndSplits pipelineNumbersAndSplits) {
    return pipelineNumbersAndSplits.getSplitInfo() == null
        ? pipelineNumbersAndSplits.getPipelineNumber()
        : String.format(
        "%s (%s)",
        pipelineNumbersAndSplits.getPipelineNumber(),
        pipelineNumbersAndSplits.getSplitInfo()
    );
  }

  private List<PipelinesAndOrgRoleGroupView> createPipelineAndOrgGroupsForRole(
      HuooRole huooRole,
      Map<PipelineIdentifier, PipelineNumbersAndSplits> pipelineNumberLookup,
      Map<OrganisationRoleOwnerDto, String> organisationRoleOwnerNameLookup,
      PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto) {

    List<PipelinesAndOrgRoleGroupView> roleGroupsMappedByRole = new ArrayList<>();

    for (PipelineAndOrganisationRoleGroupDto roleGroup : pipelineAndOrganisationRoleGroupSummaryDto.getGroupsByHuooRole(
        huooRole)) {
      var pipelineNumberList = roleGroup.getPipelineIdentifierSet().stream()
          .map(pipelineNumberLookup::get)
          .map(this::getPipelineDisplayName)
          .sorted(Comparator.comparing(String::toLowerCase))
          .collect(Collectors.toList());

      var orgNameList = roleGroup.getOrganisationRoleOwnerDtoSet().stream()
          .map(organisationRoleOwnerNameLookup::get)
          .sorted(Comparator.comparing(String::toLowerCase))
          .collect(Collectors.toList());

      var roleGroupView = new PipelinesAndOrgRoleGroupView(

          roleGroup.getPipelineIdentifierSet(),
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
