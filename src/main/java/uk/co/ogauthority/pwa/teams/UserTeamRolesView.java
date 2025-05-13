package uk.co.ogauthority.pwa.teams;

import java.util.List;
import java.util.UUID;

public record UserTeamRolesView(
    Long wuaId,
    UUID teamId,
    String teamScopeId,
    List<Role> roles
) {

  public static UserTeamRolesView from(Long wuaId, Team team, List<Role> roles) {
    return new UserTeamRolesView(
        wuaId,
        team.getId(),
        team.getScopeId(),
        roles
    );
  }
}