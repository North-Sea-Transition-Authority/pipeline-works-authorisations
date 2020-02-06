package uk.co.ogauthority.pwa.model.teams;

public enum PwaOrganisationRole {
  TEAM_ADMINISTRATOR("RESOURCE_COORDINATOR"),
  APPLICATION_SUBMITTER("APPLICATION_SUBMITTER"),
  APPLICATION_DRAFTER("APPLICATION_DRAFTER");

  private String portalTeamRoleName;

  PwaOrganisationRole(String portalTeamRoleName) {
    this.portalTeamRoleName = portalTeamRoleName;
  }

  public String getPortalTeamRoleName() {
    return portalTeamRoleName;
  }
}
