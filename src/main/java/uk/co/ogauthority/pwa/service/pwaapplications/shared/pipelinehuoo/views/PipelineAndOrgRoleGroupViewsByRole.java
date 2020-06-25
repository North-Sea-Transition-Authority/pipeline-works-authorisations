package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.List;
import org.apache.commons.collections4.ListUtils;

/**
 * Used by Pipeline Huoo overview screen to show summarised pipline huoos.
 */
public class PipelineAndOrgRoleGroupViewsByRole {

  private final List<PipelinesAndOrgRoleGroupView> holderGroups;
  private final List<PipelinesAndOrgRoleGroupView> userGroups;
  private final List<PipelinesAndOrgRoleGroupView> operatorGroups;
  private final List<PipelinesAndOrgRoleGroupView> ownerGroups;

  PipelineAndOrgRoleGroupViewsByRole(
      List<PipelinesAndOrgRoleGroupView> holderGroups,
      List<PipelinesAndOrgRoleGroupView> userGroups,
      List<PipelinesAndOrgRoleGroupView> operatorGroups,
      List<PipelinesAndOrgRoleGroupView> ownerGroups) {
    this.holderGroups = ListUtils.emptyIfNull(holderGroups);
    this.userGroups = ListUtils.emptyIfNull(userGroups);
    this.operatorGroups = ListUtils.emptyIfNull(operatorGroups);
    this.ownerGroups = ListUtils.emptyIfNull(ownerGroups);
  }

  public List<PipelinesAndOrgRoleGroupView> getHolderGroups() {
    return holderGroups;
  }

  public List<PipelinesAndOrgRoleGroupView> getUserGroups() {
    return userGroups;
  }

  public List<PipelinesAndOrgRoleGroupView> getOperatorGroups() {
    return operatorGroups;
  }

  public List<PipelinesAndOrgRoleGroupView> getOwnerGroups() {
    return ownerGroups;
  }
}
