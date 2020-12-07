package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

public class AllOrgRolePipelineGroupsView {


  private final List<OrganisationRolePipelineGroupView>  holderOrgRolePipelineGroups;
  private final List<OrganisationRolePipelineGroupView>  userOrgRolePipelineGroups;
  private final List<OrganisationRolePipelineGroupView>  operatorOrgRolePipelineGroups;
  private final List<OrganisationRolePipelineGroupView>  ownerOrgRolePipelineGroups;


  public AllOrgRolePipelineGroupsView(
      List<OrganisationRolePipelineGroupView> holderOrgRolePipelineGroups,
      List<OrganisationRolePipelineGroupView> userOrgRolePipelineGroups,
      List<OrganisationRolePipelineGroupView> operatorOrgRolePipelineGroups,
      List<OrganisationRolePipelineGroupView> ownerOrgRolePipelineGroups) {
    this.holderOrgRolePipelineGroups = holderOrgRolePipelineGroups;
    this.userOrgRolePipelineGroups = userOrgRolePipelineGroups;
    this.operatorOrgRolePipelineGroups = operatorOrgRolePipelineGroups;
    this.ownerOrgRolePipelineGroups = ownerOrgRolePipelineGroups;
  }

  public List<OrganisationRolePipelineGroupView> getHolderOrgRolePipelineGroups() {
    return holderOrgRolePipelineGroups;
  }

  public List<OrganisationRolePipelineGroupView> getUserOrgRolePipelineGroups() {
    return userOrgRolePipelineGroups;
  }

  public List<OrganisationRolePipelineGroupView> getOperatorOrgRolePipelineGroups() {
    return operatorOrgRolePipelineGroups;
  }

  public List<OrganisationRolePipelineGroupView> getOwnerOrgRolePipelineGroups() {
    return ownerOrgRolePipelineGroups;
  }

  // helper to simplify api
  public List<OrganisationRolePipelineGroupView> getOrgRolePipelineGroupView(HuooRole huooRole) {
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
        throw new RuntimeException("BAD");
    }

  }

  // TODO PWA-917 quick tests
  public boolean hasOnlyOneGroupOfPipelineIdentifiersForRole(HuooRole huooRole) {

    var countDistinctPipelineGroups = getOrgRolePipelineGroupView(huooRole)
        .stream()
        .map(OrganisationRolePipelineGroupView::getPipelineIdentifiersInGroup)
        .distinct()
        .count();

    return countDistinctPipelineGroups == 1;

  }


}


