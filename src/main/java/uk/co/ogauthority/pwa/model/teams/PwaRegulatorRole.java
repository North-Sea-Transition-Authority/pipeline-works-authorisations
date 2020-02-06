package uk.co.ogauthority.pwa.model.teams;

public enum PwaRegulatorRole {
  TEAM_ADMINISTRATOR("RESOURCE_COORDINATOR"),
  ORGANISATION_MANAGER("ORGANISATION_MANAGER");

  private String portalTeamRoleName;

  PwaRegulatorRole(String portalTeamRoleName) {
    this.portalTeamRoleName = portalTeamRoleName;
  }

  public String getPortalTeamRoleName() {
    return portalTeamRoleName;
  }
}
