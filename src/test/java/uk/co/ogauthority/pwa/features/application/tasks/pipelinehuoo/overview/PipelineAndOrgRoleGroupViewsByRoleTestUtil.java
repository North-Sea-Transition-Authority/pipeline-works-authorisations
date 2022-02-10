package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

public class PipelineAndOrgRoleGroupViewsByRoleTestUtil {

  private PipelineAndOrgRoleGroupViewsByRoleTestUtil() {
    //no instantiation
  }

  public static PipelineAndOrgRoleGroupViewsByRole createFrom(PipelineHuooRoleSummaryView holderRoleSummaryView,
                                                              PipelineHuooRoleSummaryView userRoleSumaryView,
                                                              PipelineHuooRoleSummaryView operatorRoleSummaryView,
                                                              PipelineHuooRoleSummaryView ownerRoleSummaryView) {
    return new PipelineAndOrgRoleGroupViewsByRole(
        holderRoleSummaryView,
        userRoleSumaryView,
        operatorRoleSummaryView,
        ownerRoleSummaryView);
  }

}