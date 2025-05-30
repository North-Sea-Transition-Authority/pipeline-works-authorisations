package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;

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