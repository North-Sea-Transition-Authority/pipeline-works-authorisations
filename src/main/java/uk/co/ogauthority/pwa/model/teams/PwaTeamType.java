package uk.co.ogauthority.pwa.model.teams;

import java.util.Arrays;

public enum PwaTeamType {
  REGULATOR("PWA_REGULATOR_TEAM"),
  ORGANISATION("PWA_ORGANISATION_TEAM");

  private final String portalTeamType;

  PwaTeamType(String portalTeamType) {
    this.portalTeamType = portalTeamType;
  }

  public String getPortalTeamType() {
    return portalTeamType;
  }

  public static PwaTeamType findByPortalTeamType(String portalTeamType) {
    return Arrays.stream(values())
        .filter(t -> t.getPortalTeamType().equals(portalTeamType))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Portal PwaTeam Type " + portalTeamType + " not known"));
  }
}
