package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.List;

/**
 * Used by Pipeline Huoo overview screen to show summarised pipeline huoos.
 */
public class PipelineAndOrgRoleGroupViewsByRole {

  private final PipelineHuooRoleSummaryView holderRoleSummaryView;
  private final PipelineHuooRoleSummaryView userRoleSumaryView;
  private final PipelineHuooRoleSummaryView operatorRoleSummaryView;
  private final PipelineHuooRoleSummaryView ownerRoleSummaryView;

  PipelineAndOrgRoleGroupViewsByRole(
      PipelineHuooRoleSummaryView holderRoleSummaryView,
      PipelineHuooRoleSummaryView userRoleSumaryView,
      PipelineHuooRoleSummaryView operatorRoleSummaryView,
      PipelineHuooRoleSummaryView ownerRoleSummaryView) {
    this.holderRoleSummaryView = holderRoleSummaryView;
    this.userRoleSumaryView = userRoleSumaryView;
    this.operatorRoleSummaryView = operatorRoleSummaryView;
    this.ownerRoleSummaryView = ownerRoleSummaryView;
  }

  public List<PipelinesAndOrgRoleGroupView> getHolderGroups() {
    return holderRoleSummaryView.getPipelinesAndOrgRoleGroupViews();
  }

  public List<PipelinesAndOrgRoleGroupView> getUserGroups() {
    return userRoleSumaryView.getPipelinesAndOrgRoleGroupViews();
  }

  public List<PipelinesAndOrgRoleGroupView> getOperatorGroups() {
    return operatorRoleSummaryView.getPipelinesAndOrgRoleGroupViews();
  }

  public List<PipelinesAndOrgRoleGroupView> getOwnerGroups() {
    return ownerRoleSummaryView.getPipelinesAndOrgRoleGroupViews();
  }

  public PipelineHuooRoleSummaryView getHolderRoleSummaryView() {
    return holderRoleSummaryView;
  }

  public PipelineHuooRoleSummaryView getUserRoleSummaryView() {
    return userRoleSumaryView;
  }

  public PipelineHuooRoleSummaryView getOperatorRoleSummaryView() {
    return operatorRoleSummaryView;
  }

  public PipelineHuooRoleSummaryView getOwnerRoleSummaryView() {
    return ownerRoleSummaryView;
  }

  @Override
  public String toString() {
    return "PipelineAndOrgRoleGroupViewsByRole{" +
        "holderRoleSummaryView=" + holderRoleSummaryView +
        ", userRoleSumaryView=" + userRoleSumaryView +
        ", operatorRoleSummaryView=" + operatorRoleSummaryView +
        ", ownerRoleSummaryView=" + ownerRoleSummaryView +
        '}';
  }

}
