package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;

/**
 * Used by Pipeline Huoo overview screen to show summarised pipeline huoos.
 */
public class PipelineAndOrgRoleGroupViewsByRole {

  private final PipelineHuooRoleSummaryView holderRoleSummaryView;
  private final PipelineHuooRoleSummaryView userRoleSummaryView;
  private final PipelineHuooRoleSummaryView operatorRoleSummaryView;
  private final PipelineHuooRoleSummaryView ownerRoleSummaryView;

  PipelineAndOrgRoleGroupViewsByRole(
      PipelineHuooRoleSummaryView holderRoleSummaryView,
      PipelineHuooRoleSummaryView userRoleSummaryView,
      PipelineHuooRoleSummaryView operatorRoleSummaryView,
      PipelineHuooRoleSummaryView ownerRoleSummaryView) {
    this.holderRoleSummaryView = holderRoleSummaryView;
    this.userRoleSummaryView = userRoleSummaryView;
    this.operatorRoleSummaryView = operatorRoleSummaryView;
    this.ownerRoleSummaryView = ownerRoleSummaryView;
  }

  public List<PipelinesAndOrgRoleGroupView> getHolderGroups() {
    return holderRoleSummaryView.getPipelinesAndOrgRoleGroupViews();
  }

  public List<PipelinesAndOrgRoleGroupView> getUserGroups() {
    return userRoleSummaryView.getPipelinesAndOrgRoleGroupViews();
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
    return userRoleSummaryView;
  }

  public PipelineHuooRoleSummaryView getOperatorRoleSummaryView() {
    return operatorRoleSummaryView;
  }

  public PipelineHuooRoleSummaryView getOwnerRoleSummaryView() {
    return ownerRoleSummaryView;
  }

  public PipelineHuooRoleSummaryView getRoleSummaryView(HuooRole huooRole) {
    return switch (huooRole) {
      case HOLDER -> holderRoleSummaryView;
      case USER -> userRoleSummaryView;
      case OPERATOR -> operatorRoleSummaryView;
      case OWNER -> ownerRoleSummaryView;
    };
  }

  @Override
  public String toString() {
    return "PipelineAndOrgRoleGroupViewsByRole{" +
        "holderRoleSummaryView=" + holderRoleSummaryView +
        ", userRoleSumaryView=" + userRoleSummaryView +
        ", operatorRoleSummaryView=" + operatorRoleSummaryView +
        ", ownerRoleSummaryView=" + ownerRoleSummaryView +
        '}';
  }

}
