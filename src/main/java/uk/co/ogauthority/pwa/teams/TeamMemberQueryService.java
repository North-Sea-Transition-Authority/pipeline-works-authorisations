package uk.co.ogauthority.pwa.teams;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.UsersProjectionRoot;
import uk.co.ogauthority.pwa.teams.management.TeamManagementException;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

@Service
public class TeamMemberQueryService {
  private final TeamRoleRepository teamRoleRepository;
  private final UserApi userApi;
  private final TeamRepository teamRepository;

  @Autowired
  public TeamMemberQueryService(TeamRoleRepository teamRoleRepository,
                                UserApi userApi,
                                TeamRepository teamRepository) {
    this.teamRoleRepository = teamRoleRepository;
    this.userApi = userApi;
    this.teamRepository = teamRepository;
  }

  public TeamMemberView getTeamMemberView(Team team, Long wuaId) {
    var teamRoles = teamRoleRepository.findByWuaIdAndTeam(wuaId, team).stream()
        .map(TeamRole::getRole)
        .toList();

    var userProjection = new UserProjectionRoot()
        .webUserAccountId()
        .title()
        .forename()
        .surname()
        .primaryEmailAddress()
        .telephoneNumber();

    var user = userApi.findUserById(Math.toIntExact(wuaId), userProjection,
            new RequestPurpose("Fetch user in team"))
        .orElseThrow(() -> new TeamManagementException("WuaId %s not found via EPA".formatted(wuaId)));

    return TeamMemberView.fromEpaUser(user, team.getId(), teamRoles);
  }

  public List<TeamMemberView> getTeamMemberViewsForTeam(Team team) {
    var teamRoles = teamRoleRepository.findByTeam(team);

    var memberWuaIds = teamRoles.stream()
        .map(TeamRole::getWuaId)
        .distinct()
        .toList();

    var userProjection = new UsersProjectionRoot()
        .webUserAccountId()
        .title()
        .forename()
        .surname()
        .primaryEmailAddress()
        .telephoneNumber();

    var memberWuaIdInts = memberWuaIds.stream()
        .map(Math::toIntExact)
        .toList();
    var epaUsers = userApi.searchUsersByIds(memberWuaIdInts, userProjection,
        new RequestPurpose("Fetch users in team"));

    return memberWuaIds.stream()
        .map(wuaId -> {
          var epaUser = epaUsers.stream()
              .filter(u -> u.getWebUserAccountId().equals(Math.toIntExact(wuaId)))
              .findFirst()
              .orElseThrow(() -> new TeamManagementException("WuaId %s not found in EPA user set".formatted(wuaId)));

          List<Role> userRoles = teamRoles.stream()
              .filter(teamRole -> teamRole.getWuaId().equals(wuaId))
              .map(TeamRole::getRole)
              .toList();

          List<Role> orderedUserRoles = team.getTeamType().getAllowedRoles()
              .stream()
              .filter(userRoles::contains)
              .toList();

          return TeamMemberView.fromEpaUser(epaUser, team.getId(), orderedUserRoles);
        })
        .sorted(Comparator.comparing(TeamMemberView::forename).thenComparing(TeamMemberView::surname))
        .toList();
  }

  List<TeamMemberView> getTeamMemberViewsByTeamAndRole(Team team, Role role) {
    return teamRoleRepository.findByTeamAndRole(team, role).stream()
        .map(teamRole -> getTeamMemberView(teamRole.getTeam(), teamRole.getWuaId()))
        .toList();
  }

  public List<TeamMemberView> getTeamMemberViewsByScopedTeam(TeamType teamType, TeamScopeReference teamScopeReference) {
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, teamScopeReference.getType(), teamScopeReference.getId())
        .stream()
        .flatMap(team -> getTeamMemberViewsForTeam(team).stream())
        .toList();
  }
}