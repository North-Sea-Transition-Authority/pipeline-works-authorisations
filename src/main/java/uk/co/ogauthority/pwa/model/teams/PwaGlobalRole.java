package uk.co.ogauthority.pwa.model.teams;

public enum PwaGlobalRole {

  PWA_ACCESS("PWA_ACCESS");

  private final String portalTeamRoleName;

  PwaGlobalRole(String portalTeamRoleName) {
    this.portalTeamRoleName = portalTeamRoleName;
  }

  public String getPortalTeamRoleName() {
    return portalTeamRoleName;
  }

}
