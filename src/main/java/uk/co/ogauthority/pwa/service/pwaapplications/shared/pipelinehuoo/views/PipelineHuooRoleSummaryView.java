package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

public class PipelineHuooRoleSummaryView {

  private final HuooRole huooRole;

  private final List<PipelinesAndOrgRoleGroupView> pipelinesAndOrgRoleGroupViews;

  private final Map<PipelineId, String> unassignedPipelineNumberMapForRole;

  private final Map<OrganisationUnitId, String> unassignedOrganisationNameMapForRole;

  private final List<String> sortedUnassignedOrganisationNames;
  private final List<String> sortedUnassignedPipelineNumbers;

  public PipelineHuooRoleSummaryView(HuooRole huooRole,
                                     List<PipelinesAndOrgRoleGroupView> pipelinesAndOrgRoleGroupViews,
                                     Map<PipelineId, String> unassignedPipelineNumberMapForRole,
                                     Map<OrganisationUnitId, String> unassignedOrganisationNameMapForRole) {
    this.huooRole = huooRole;
    this.pipelinesAndOrgRoleGroupViews = pipelinesAndOrgRoleGroupViews;
    this.unassignedPipelineNumberMapForRole = unassignedPipelineNumberMapForRole;
    this.unassignedOrganisationNameMapForRole = unassignedOrganisationNameMapForRole;

    sortedUnassignedOrganisationNames = this.unassignedOrganisationNameMapForRole.values()
        .stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());

    sortedUnassignedPipelineNumbers = this.unassignedPipelineNumberMapForRole.values()
        .stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());
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

  Map<OrganisationUnitId, String> getUnassignedOrganisationNameMapForRole() {
    return Collections.unmodifiableMap(unassignedOrganisationNameMapForRole);
  }

  public List<String> getSortedUnassignedOrganisationNames() {
    return sortedUnassignedOrganisationNames;
  }

  public List<String> getSortedUnassignedPipelineNumbers() {
    return sortedUnassignedPipelineNumbers;
  }
}
