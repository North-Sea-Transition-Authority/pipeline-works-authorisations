package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.mh_debug_prototype;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffableOrgRolePipelineGroup;

// mirrors AllOrgRolePipelineGroupsView but has done post processing of group views for template consumption
// TODO PWA-917 (wishlist) this object and DiffableOrgRolePipelineGroup should be renamed so its more generic. Is not always required for Diff,
// sometimes required on its own, so should no bake in "Diff" naming.
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
    }
    throw new RuntimeException("BAD");
  }


  //TODO PWA-917 getters

}


