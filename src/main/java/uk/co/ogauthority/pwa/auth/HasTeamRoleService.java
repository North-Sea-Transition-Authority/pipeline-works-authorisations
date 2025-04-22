package uk.co.ogauthority.pwa.auth;

import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class HasTeamRoleService {


  private final TeamQueryService teamQueryService;

  public HasTeamRoleService(TeamQueryService teamQueryService) {
    this.teamQueryService = teamQueryService;
  }

  public boolean userHasAnyRoleInTeamType(AuthenticatedUserAccount user, TeamType teamType, Set<Role> roles) {
    return userHasAnyRoleInTeamTypes(user, Map.of(teamType, roles));
  }

  public boolean userHasAnyRoleInTeamTypes(AuthenticatedUserAccount user, Map<TeamType, Set<Role>> rolesByTeamTypeMap) {
    return rolesByTeamTypeMap.entrySet().stream()
        .anyMatch(entry -> teamQueryService.userHasAtLeastOneRole((long) user.getWuaId(), entry.getKey(), entry.getValue()));
  }

  public boolean userIsMemberOfAnyTeam(AuthenticatedUserAccount user) {
    return teamQueryService.userIsMemberOfAnyTeam(user.getWuaId());
  }
}
