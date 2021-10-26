package uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices;

import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffableOrgRolePipelineGroup;

// mirrors AllOrgRolePipelineGroupsView but has done post processing of group views for template consumption
// TODO PWA-917 (wishlist) this object and DiffableOrgRolePipelineGroup should be renamed so its more generic.
//  Is not always required for Diff, sometimes required on its own, so should no bake in "Diff" naming.
public class AllRoleDiffablePipelineGroupView {


  private final List<DiffableOrgRolePipelineGroup>  holderOrgRolePipelineGroups;
  private final List<DiffableOrgRolePipelineGroup>  userOrgRolePipelineGroups;
  private final List<DiffableOrgRolePipelineGroup>  operatorOrgRolePipelineGroups;
  private final List<DiffableOrgRolePipelineGroup>  ownerOrgRolePipelineGroups;


  public AllRoleDiffablePipelineGroupView(
      List<DiffableOrgRolePipelineGroup> holderOrgRolePipelineGroups,
      List<DiffableOrgRolePipelineGroup> userOrgRolePipelineGroups,
      List<DiffableOrgRolePipelineGroup> operatorOrgRolePipelineGroups,
      List<DiffableOrgRolePipelineGroup> ownerOrgRolePipelineGroups) {
    this.holderOrgRolePipelineGroups = holderOrgRolePipelineGroups;
    this.userOrgRolePipelineGroups = userOrgRolePipelineGroups;
    this.operatorOrgRolePipelineGroups = operatorOrgRolePipelineGroups;
    this.ownerOrgRolePipelineGroups = ownerOrgRolePipelineGroups;
  }


  // helper to simplify api
  private List<DiffableOrgRolePipelineGroup> getOrgRolePipelineGroupView(HuooRole huooRole) {
    switch (huooRole) {
      case HOLDER:
        return this.holderOrgRolePipelineGroups;
      case USER:
        return this.userOrgRolePipelineGroups;
      case OPERATOR:
        return this.operatorOrgRolePipelineGroups;
      case OWNER:
        return this.ownerOrgRolePipelineGroups;
      default:
        throw new RuntimeException("Invalid huoo role provided");
    }
  }


  public List<DiffableOrgRolePipelineGroup> getHolderOrgRolePipelineGroups() {
    return holderOrgRolePipelineGroups;
  }

  public List<DiffableOrgRolePipelineGroup> getUserOrgRolePipelineGroups() {
    return userOrgRolePipelineGroups;
  }

  public List<DiffableOrgRolePipelineGroup> getOperatorOrgRolePipelineGroups() {
    return operatorOrgRolePipelineGroups;
  }

  public List<DiffableOrgRolePipelineGroup> getOwnerOrgRolePipelineGroups() {
    return ownerOrgRolePipelineGroups;
  }


}


