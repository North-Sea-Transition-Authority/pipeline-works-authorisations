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
        .anyMatch(teamRole -> teamTypeMatchesTeam(teamType, teamRole.getTeam()));
  }

  public boolean userIsMemberOfScopedTeam(Long wuaId, TeamType teamType, TeamScopeReference teamScopeReference) {
    assertTeamTypeIsScoped(teamType);

    return teamRoleRepository.findAllByWuaId(wuaId).stream()
        .anyMatch(teamRole -> teamTypeMatchesTeam(teamType, teamRole.getTeam())
            && teamScopeReferenceMatchesTeam(teamScopeReference, teamRole.getTeam()));
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
        .anyMatch(teamRole -> teamTypeMatchesTeam(teamType, teamRole.getTeam()) && roles.contains(teamRole.getRole()));
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

  public List<UserTeamRolesView> getUsersOfScopedTeam(TeamType teamType, TeamScopeReference teamScopeReference) {
    assertTeamTypeIsScoped(teamType);

    var teamRoles = teamRoleRepository.findAllByTeam_TeamType(teamType)
        .stream()
        .filter(teamRole -> teamScopeReferenceMatchesTeam(teamScopeReference, teamRole.getTeam()))
        .toList();

    return teamMemberQueryService.getUserTeamRolesViewsFrom(teamRoles);
  }

  public List<UserTeamRolesView> getUsersOfTeam(Team team) {
    var teamRoles = teamRoleRepository.findByTeam(team);
    return teamMemberQueryService.getUserTeamRolesViewsFrom(teamRoles);
  }

  public List<TeamMemberView> getMembersOfStaticTeamWithRole(TeamType teamType, Role role) {
    var team = getStaticTeamByTeamType(teamType);

    return teamMemberQueryService.getTeamMemberViewsByTeamAndRole(team, role);
  }

  public List<UserTeamRolesView> getUsersOfStaticTeamWithRole(TeamType teamType, Role role) {
    var team = getStaticTeamByTeamType(teamType);

    var teamRoles = teamRoleRepository.findByTeamAndRole(team, role);

    return teamMemberQueryService.getUserTeamRolesViewsFrom(teamRoles);
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

  public List<UserTeamRolesView> getTeamRolesViewsByUserAndTeamType(long wuaId, TeamType teamType) {
    var teamRoles = teamRoleRepository.findAllByWuaId(wuaId)
        .stream()
        .filter(teamRole -> teamTypeMatchesTeam(teamType, teamRole.getTeam()))
        .toList();

    return teamMemberQueryService.getUserTeamRolesViewsFrom(teamRoles);
  }

  public Set<Role> getRolesForUserInScopedTeams(long wuaId,
                                                TeamType teamType,
                                                Collection<String> scopeIds) {
    assertTeamTypeIsScoped(teamType);

    return teamRoleRepository.findAllByWuaId(wuaId).stream()
        .filter(teamRole -> {
          var team = teamRole.getTeam();
          return teamTypeMatchesTeam(teamType, team) && scopeIds.contains(team.getScopeId());
        })
        .map(TeamRole::getRole)
        .collect(Collectors.toSet());
  }

  public List<TeamMemberView> getMembersOfScopedTeams(TeamType teamType, Collection<String> scopeIds) {
    assertTeamTypeIsScoped(teamType);

    var teamRoles = getTeamRolesByTeamTypeAndScopeIdsIn(teamType, scopeIds);

    return teamMemberQueryService.getTeamMemberViewsByTeamRoles(teamRoles);
  }

  public List<UserTeamRolesView> getUsersOfScopedTeams(TeamType teamType, Collection<String> scopeIds) {
    assertTeamTypeIsScoped(teamType);

    var teamRoles = getTeamRolesByTeamTypeAndScopeIdsIn(teamType, scopeIds);

    return teamMemberQueryService.getUserTeamRolesViewsFrom(teamRoles);
  }

  public List<TeamMemberView> getMembersOfTeamTypeWithRoleIn(TeamType teamType, Collection<Role> roles) {
    var teamRoles = getTeamRolesByRolesIn(teamType, roles);

    return teamMemberQueryService.getTeamMemberViewsByTeamRoles(teamRoles);
  }

  public List<UserTeamRolesView> getUsersOfTeamTypeWithRoleIn(TeamType teamType, Collection<Role> roles) {
    var teamRoles = getTeamRolesByRolesIn(teamType, roles);

    return teamMemberQueryService.getUserTeamRolesViewsFrom(teamRoles);
  }

  public List<TeamMemberView> getMembersOfScopedTeamWithRoleIn(TeamType teamType,
                                                               TeamScopeReference teamScopeReference,
                                                               Collection<Role> roles) {
    assertTeamTypeIsScoped(teamType);

    var teamRoles = getTeamRolesByScopeReferenceAndRolesIn(teamType, teamScopeReference, roles);

    return teamMemberQueryService.getTeamMemberViewsByTeamRoles(teamRoles);
  }

  public List<UserTeamRolesView> getUsersOfScopedTeamWithRoleIn(TeamType teamType,
                                                               TeamScopeReference teamScopeReference,
                                                               Collection<Role> roles) {
    assertTeamTypeIsScoped(teamType);

    var teamRoles = getTeamRolesByScopeReferenceAndRolesIn(teamType, teamScopeReference, roles);

    return teamMemberQueryService.getUserTeamRolesViewsFrom(teamRoles);
  }

  private List<TeamRole> getTeamRolesByTeamTypeAndScopeIdsIn(TeamType teamType, Collection<String> scopeIds) {
    return teamRoleRepository.findAllByTeam_TeamType(teamType).stream()
        .filter(team -> team.getTeam().getScopeId() != null)
        .filter(team -> scopeIds.contains(team.getTeam().getScopeId()))
        .toList();
  }

  private List<TeamRole> getTeamRolesByRolesIn(TeamType teamType, Collection<Role> roles) {
    return teamRoleRepository.findAllByTeam_TeamType(teamType)
        .stream()
        .filter(teamRole -> roles.contains(teamRole.getRole()))
        .toList();
  }

  private List<TeamRole> getTeamRolesByScopeReferenceAndRolesIn(TeamType teamType,
                                                                TeamScopeReference teamScopeReference,
                                                                Collection<Role> roles) {
    return teamRoleRepository.findAllByTeam_TeamType(teamType)
        .stream()
        .filter(teamRole -> teamScopeReferenceMatchesTeam(teamScopeReference, teamRole.getTeam()))
        .filter(teamRole -> roles.contains(teamRole.getRole()))
        .toList();
  }

  private boolean teamScopeReferenceMatchesTeam(TeamScopeReference teamScopeReference, Team team) {
    return teamScopeReference.getId().equals(team.getScopeId())
        && teamScopeReference.getType().equals(team.getScopeType());
  }

  private boolean teamTypeMatchesTeam(TeamType teamType, Team team) {
    return team.getTeamType() == teamType;
  }
}
