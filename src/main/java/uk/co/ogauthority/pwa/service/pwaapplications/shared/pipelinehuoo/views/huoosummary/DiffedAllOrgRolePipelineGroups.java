package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary;

import java.util.List;
import java.util.Map;

public class DiffedAllOrgRolePipelineGroups {

  private final List<Map<String, ?>>  holderOrgRolePipelineGroups;
  private final List<Map<String, ?>>  userOrgRolePipelineGroups;
  private final List<Map<String, ?>>  operatorOrgRolePipelineGroups;
  private final List<Map<String, ?>>  ownerOrgRolePipelineGroups;

  public DiffedAllOrgRolePipelineGroups(List<Map<String, ?>> holderOrgRolePipelineGroups,
                                        List<Map<String, ?>> userOrgRolePipelineGroups,
                                        List<Map<String, ?>> operatorOrgRolePipelineGroups,
                                        List<Map<String, ?>> ownerOrgRolePipelineGroups) {
    this.holderOrgRolePipelineGroups = holderOrgRolePipelineGroups;
    this.userOrgRolePipelineGroups = userOrgRolePipelineGroups;
    this.operatorOrgRolePipelineGroups = operatorOrgRolePipelineGroups;
    this.ownerOrgRolePipelineGroups = ownerOrgRolePipelineGroups;
  }


  public List<Map<String, ?>> getHolderOrgRolePipelineGroups() {
    return holderOrgRolePipelineGroups;
  }

  public List<Map<String, ?>> getUserOrgRolePipelineGroups() {
    return userOrgRolePipelineGroups;
  }

  public List<Map<String, ?>> getOperatorOrgRolePipelineGroups() {
    return operatorOrgRolePipelineGroups;
  }

  public List<Map<String, ?>> getOwnerOrgRolePipelineGroups() {
    return ownerOrgRolePipelineGroups;
  }


}
