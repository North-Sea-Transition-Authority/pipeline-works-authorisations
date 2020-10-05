package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary;

import java.util.List;

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
}


