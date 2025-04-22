package uk.co.ogauthority.pwa.teams;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

@Service
public class TeamQueryService {
  private final TeamRepository teamRepository;
  private final TeamRoleRepository teamRoleRepository;
  private final TeamMemberQueryService teamMemberQueryService;

  public TeamQueryService(TeamRepository teamRepository,
                          TeamRoleRepository teamRoleRepository,
                          TeamMemberQueryService teamMemberQueryService) {
    this.teamRepository = teamRepository;
    this.teamRoleRepository = teamRoleRepository;
    this.teamMemberQueryService = teamMemberQueryService;
  }

  public boolean userIsMemberOfStaticTeam(Long wuaId, TeamType teamType) {
    var team = getStaticTeamByTeamType(teamType);

    return teamRoleRepository.existsByTeamAndWuaId(team, wuaId);
  }

  public boolean userIsMemberOfAnyScopedTeamOfType(Long wuaId, TeamType teamType) {
    assertTeamTypeIsScoped(teamType);

    return teamRoleRepository.findAllByWuaId(wuaId).stream()
        .anyMatch(teamRole -> teamRole.getTeam().getTeamType() == teamType);
  }

  public boolean userHasStaticRole(Long wuaId, TeamType teamType, Role role) {
    return userHasAtLeastOneStaticRole(wuaId, teamType, Set.of(role));
  }

  public boolean userHasAtLeastOneStaticRole(Long wuaId, TeamType teamType, Set<Role> roles) {
    assertRolesValidForTeamType(roles, teamType);
    assertTeamTypeIsStatic(teamType);

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
    assertTeamTypeIsScoped(teamType);
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId())
        .filter(team -> userHasAtLeastOneRole(wuaId, team, roles))
        .isPresent();
  }

  private boolean userHasAtLeastOneRole(Long wuaId, Team team, Set<Role> roles) {
    return teamRoleRepository.findByWuaIdAndTeam(wuaId, team).stream()
        .anyMatch(teamRole -> roles.contains(teamRole.getRole()));
  }

  public boolean userHasAtLeastOneRole(Long wuaId, TeamType teamType, Set<Role> roles) {
    assertRolesValidForTeamType(roles, teamType);

    return teamRoleRepository.findAllByWuaId(wuaId).stream()
        .anyMatch(teamRole -> teamRole.getTeam().getTeamType() == teamType && roles.contains(teamRole.getRole()));
  }

  private void assertRolesValidForTeamType(Set<Role> roles, TeamType teamType) {
    roles.forEach(role -> {
      if (!teamType.getAllowedRoles().contains(role)) {
        throw new IllegalArgumentException("Role %s is not valid for TeamType %s".formatted(role, teamType));
      }
    });
  }

  private void assertTeamTypeIsScoped(TeamType teamType) {
    if (!teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not scoped".formatted(teamType));
    }
  }

  private void assertTeamTypeIsStatic(TeamType teamType) {
    if (teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not static".formatted(teamType));
    }
  }

  public boolean userIsMemberOfAnyTeam(long wuaId) {
    var teamRoles = teamRoleRepository.findAllByWuaId(wuaId);
    return !teamRoles.isEmpty();
  }

  public List<TeamMemberView> getMembersOfScopedTeam(TeamType teamType, TeamScopeReference teamScopeReference) {
    assertTeamTypeIsScoped(teamType);
    return teamMemberQueryService.getTeamMemberViewsByScopedTeam(teamType, teamScopeReference);
  }

  public List<TeamMemberView> getMembersOfTeam(Team team) {
    return teamMemberQueryService.getTeamMemberViewsForTeam(team);
  }

  public List<TeamMemberView> getMembersOfStaticTeamWithRole(TeamType teamType, Role role) {
    var team = getStaticTeamByTeamType(teamType);

    return teamMemberQueryService.getTeamMemberViewsByTeamAndRole(team, role);
  }

  @VisibleForTesting
  Team getStaticTeamByTeamType(TeamType teamType) {
    assertTeamTypeIsStatic(teamType);

    return teamRepository.findByTeamType(teamType).stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No team found for static team of TeamType %s".formatted(teamType)));
  }

  public List<Team> getTeamsOfTypeUserHasAnyRoleIn(long wuaId, TeamType teamType, Collection<Role> roles) {
    return teamRoleRepository.findByWuaIdAndTeam_TeamTypeAndRoleIn(wuaId, teamType, roles).stream()
        .map(TeamRole::getTeam)
        .distinct()
        .toList();

  }

  public List<Team> getTeamsUserIsMemberOf(long wuaId) {
    return teamRoleRepository.findAllByWuaId(wuaId).stream()
        .map(TeamRole::getTeam)
        .distinct()
        .toList();

  }

  public Set<Role> getRolesForUserInScopedTeams(long wuaId,
                                                TeamType teamType,
                                                Collection<String> scopeIds) {
    assertTeamTypeIsScoped(teamType);

    return teamRoleRepository.findAllByWuaId(wuaId).stream()
        .filter(teamRole -> {
          var team = teamRole.getTeam();
          return team.getTeamType() == teamType && scopeIds.contains(team.getScopeId());
        })
        .map(TeamRole::getRole)
        .collect(Collectors.toSet());
  }

  public Set<Team> getScopedTeamsByScopeIds(TeamType teamType, Collection<String> scopeIds) {
    assertTeamTypeIsScoped(teamType);
    return teamRepository.findAllByTeamTypeAndScopeTypeAndScopeIdIn(teamType, teamType.getScopeType(), scopeIds);
  }
}
