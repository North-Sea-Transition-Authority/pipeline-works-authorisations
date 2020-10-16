package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

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