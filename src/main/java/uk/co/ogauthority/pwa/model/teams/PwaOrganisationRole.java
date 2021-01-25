package uk.co.ogauthority.pwa.model.teams;

public enum PwaOrganisationRole {

  TEAM_ADMINISTRATOR("RESOURCE_COORDINATOR"),
  APPLICATION_CREATOR("APPLICATION_CREATE");

  private final String portalTeamRoleName;

  PwaOrganisationRole(String portalTeamRoleName) {
    this.portalTeamRoleName = portalTeamRoleName;
  }

  public String getPortalTeamRoleName() {
    return portalTeamRoleName;
  }
}
