package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

public class PipelineHuooRoleSummaryView {

  private final HuooRole huooRole;

  private final List<PipelinesAndOrgRoleGroupView> pipelinesAndOrgRoleGroupViews;

  private final Map<PipelineId, String> unassignedPipelineNumberMapForRole;

  private final Map<OrganisationRoleOwnerDto, String> unassignedOrganisationRoleOwnerNameMapForRole;

  private final List<String> sortedUnassignedOrganisationNames;
  private final List<String> sortedUnassignedPipelineNumbers;

  PipelineHuooRoleSummaryView(HuooRole huooRole,
                              List<PipelinesAndOrgRoleGroupView> pipelinesAndOrgRoleGroupViews,
                              Map<PipelineId, String> unassignedPipelineNumberMapForRole,
                              Map<OrganisationRoleOwnerDto, String> unassignedOrganisationRoleOwnerNameMapForRole) {
    this.huooRole = huooRole;
    this.pipelinesAndOrgRoleGroupViews = pipelinesAndOrgRoleGroupViews.stream()
        .sorted(Comparator.comparing(PipelinesAndOrgRoleGroupView::getSortKey))
        .collect(Collectors.toList());
    this.unassignedPipelineNumberMapForRole = unassignedPipelineNumberMapForRole;
    this.unassignedOrganisationRoleOwnerNameMapForRole = unassignedOrganisationRoleOwnerNameMapForRole;

    sortedUnassignedOrganisationNames = this.unassignedOrganisationRoleOwnerNameMapForRole.values()
        .stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());

    sortedUnassignedPipelineNumbers = this.unassignedPipelineNumberMapForRole.values()
        .stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());
  }

  public Set<PipelineId> getUnassignedPipelineId() {
    return this.unassignedPipelineNumberMapForRole.keySet();
  }

  public Set<OrganisationUnitId> getUnassignedRoleOwnerOrganisationIds() {
    return this.unassignedOrganisationRoleOwnerNameMapForRole.keySet().stream()
        .filter(o -> HuooType.PORTAL_ORG.equals(o.getHuooType()))
        .map(OrganisationRoleOwnerDto::getOrganisationUnitId)
        .collect(Collectors.toSet());
  }

  public Set<TreatyAgreement> getUnassignedRoleOwnerTreatyAgreements() {
    return this.unassignedOrganisationRoleOwnerNameMapForRole.keySet().stream()
        .filter(o -> HuooType.TREATY_AGREEMENT.equals(o.getHuooType()))
        .map(OrganisationRoleOwnerDto::getTreatyAgreement)
        .collect(Collectors.toSet());
  }

  public HuooRole getHuooRole() {
    return huooRole;
  }

  public String getRoleDisplayText() {
    return this.huooRole.getDisplayText();
  }

  public List<PipelinesAndOrgRoleGroupView> getPipelinesAndOrgRoleGroupViews() {
    return Collections.unmodifiableList(pipelinesAndOrgRoleGroupViews);
  }

  Map<PipelineId, String> getUnassignedPipelineNumberMapForRole() {
    return Collections.unmodifiableMap(unassignedPipelineNumberMapForRole);
  }

  Map<OrganisationRoleOwnerDto, String> getUnassignedOrganisationRoleOwnerNameMapForRole() {
    return Collections.unmodifiableMap(unassignedOrganisationRoleOwnerNameMapForRole);
  }

  public List<String> getSortedUnassignedOrganisationNames() {
    return sortedUnassignedOrganisationNames;
  }

  public List<String> getSortedUnassignedPipelineNumbers() {
    return sortedUnassignedPipelineNumbers;
  }
}
