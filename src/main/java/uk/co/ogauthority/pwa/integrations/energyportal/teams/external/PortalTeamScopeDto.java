package uk.co.ogauthority.pwa.integrations.energyportal.teams.external;

import java.util.Objects;

public class PortalTeamScopeDto {

  private final String primaryScope;

  public PortalTeamScopeDto(String primaryScope) {
    this.primaryScope = primaryScope;
  }

  public String getPrimaryScope() {
    return primaryScope;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalTeamScopeDto)) {
      return false;
    }
    PortalTeamScopeDto that = (PortalTeamScopeDto) o;
    return Objects.equals(primaryScope, that.primaryScope);
  }

  @Override
  public int hashCode() {
    return Objects.hash(primaryScope);
  }
}
