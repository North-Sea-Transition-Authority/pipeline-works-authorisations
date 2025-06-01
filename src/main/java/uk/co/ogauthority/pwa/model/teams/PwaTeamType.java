package uk.co.ogauthority.pwa.model.teams;

public enum PwaTeamType {

  REGULATOR("PWA_REGULATOR_TEAM", "PWA regulator team"),
  ORGANISATION("PWA_ORGANISATION_TEAM", "PWA organisation team"),
  GLOBAL("PWA_USERS", "PWA users");

  private final String portalTeamType;
  private final String portalTeamTypeDisplayName;

  PwaTeamType(String portalTeamType, String portalTeamTypeDisplayName) {
    this.portalTeamType = portalTeamType;
    this.portalTeamTypeDisplayName = portalTeamTypeDisplayName;
  }

  public String getPortalTeamType() {
    return portalTeamType;
  }

  public String getPortalTeamTypeDisplayName() {
    return portalTeamTypeDisplayName;
  }
}
