package uk.co.ogauthority.pwa.teams;

import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TeamQueryService {
  private final TeamRepository teamRepository;
  private final TeamRoleRepository teamRoleRepository;

  public TeamQueryService(TeamRepository teamRepository, TeamRoleRepository teamRoleRepository) {
    this.teamRepository = teamRepository;
    this.teamRoleRepository = teamRoleRepository;
  }

  public boolean userHasStaticRole(Long wuaId, TeamType teamType, Role role) {
    return userHasAtLeastOneStaticRole(wuaId, teamType, Set.of(role));
  }

  public boolean userHasAtLeastOneStaticRole(Long wuaId, TeamType teamType, Set<Role> roles) {
    assertRolesValidForTeamType(roles, teamType);
    if (teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not static".formatted(teamType));
    }

    return teamRepository.findByTeamType(teamType).stream()
        .findFirst()
        .filter(team -> userHasAtLeastOneRole(wuaId, team, roles))
        .isPresent();
  }

  public boolean userHasScopedRole(Long wuaId, TeamType teamType, TeamScopeReference scopeRef, Role role) {
    return userHasAtLeastOneScopedRole(wuaId, teamType, scopeRef, Set.of(role));
  }

  public boolean userHasAtLeastOneScopedRole(Long wuaId, TeamType teamType, TeamScopeReference scopeRef, Set<Role> roles) {
    assertRolesValidForTeamType(roles, teamType);
    if (!teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not scoped".formatted(teamType));
    }
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId())
        .filter(team -> userHasAtLeastOneRole(wuaId, team, roles))
        .isPresent();
  }

  private boolean userHasAtLeastOneRole(Long wuaId, Team team, Set<Role> roles) {
    return teamRoleRepository.findByWuaIdAndTeam(wuaId, team).stream()
        .anyMatch(teamRole -> roles.contains(teamRole.getRole()));
  }

  private void assertRolesValidForTeamType(Set<Role> roles, TeamType teamType) {
    roles.forEach(role -> {
      if (!teamType.getAllowedRoles().contains(role)) {
        throw new IllegalArgumentException("Role %s is not valid for TeamType %s".formatted(role, teamType));
      }
    });
  }

}
