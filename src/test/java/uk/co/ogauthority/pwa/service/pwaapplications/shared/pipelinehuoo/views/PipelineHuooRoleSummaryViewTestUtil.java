package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

public class PipelineHuooRoleSummaryViewTestUtil {
  private PipelineHuooRoleSummaryViewTestUtil() {
    // no instantiation
  }

  public static PipelineHuooRoleSummaryView createWithNoUnassignedPipelinesOrOrgs(HuooRole huooRole,
                                                                                  List<PipelinesAndOrgRoleGroupView> pipelinesAndOrgRoleGroupViews) {
    return new PipelineHuooRoleSummaryView(
        huooRole,
        pipelinesAndOrgRoleGroupViews,
        Collections.emptyMap(),
        Collections.emptyMap());
  }

  public static PipelineHuooRoleSummaryView createEmptyGroupWithNoUnassigned(HuooRole huooRole) {
    return new PipelineHuooRoleSummaryView(
        huooRole,
        Collections.emptyList(),
        Collections.emptyMap(),
        Collections.emptyMap());
  }

  public static PipelineHuooRoleSummaryView createEmptyGroupWithUnassigned(HuooRole huooRole,
                                                                           Map<PipelineIdentifier, String> unassignedPipelines,
                                                                           Map<OrganisationRoleOwnerDto, String> unassignedRoleOwners) {
    return new PipelineHuooRoleSummaryView(
        huooRole,
        Collections.emptyList(),
        unassignedPipelines,
        unassignedRoleOwners);
  }
}