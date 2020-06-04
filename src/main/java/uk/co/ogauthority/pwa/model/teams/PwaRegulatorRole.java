package uk.co.ogauthority.pwa.model.teams;

import java.util.stream.Stream;

public enum PwaRegulatorRole {

  TEAM_ADMINISTRATOR("RESOURCE_COORDINATOR"),
  ORGANISATION_MANAGER("ORGANISATION_MANAGER"),
  PWA_MANAGER("PWA_MANAGER"),
  CASE_OFFICER("CASE_OFFICER");

  private final String portalTeamRoleName;

  PwaRegulatorRole(String portalTeamRoleName) {
    this.portalTeamRoleName = portalTeamRoleName;
  }

  public String getPortalTeamRoleName() {
    return portalTeamRoleName;
  }

  public static PwaRegulatorRole getValueByPortalTeamRoleName(String portalTeamRoleName) {
    return Stream.of(PwaRegulatorRole.values())
        .filter(r -> r.getPortalTeamRoleName().equals(portalTeamRoleName))
        .findFirst()
        .orElseThrow(() -> new RuntimeException(String.format(
            "Couldn't map portal team role name: %s to a PwaRegulatorRole value", portalTeamRoleName)));
  }

}
