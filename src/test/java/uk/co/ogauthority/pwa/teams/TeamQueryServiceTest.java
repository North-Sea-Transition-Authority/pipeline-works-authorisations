package uk.co.ogauthority.pwa.teams;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

@ExtendWith(MockitoExtension.class)
class TeamQueryServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamRoleRepository teamRoleRepository;

  @Mock
  private TeamMemberQueryService teamMemberQueryService;

  @Spy
  @InjectMocks
  private TeamQueryService teamQueryService;

  @Test
  void userIsMemberOfStaticTeam_isMember() {

    Team team = mock(Team.class);
    setupStaticTeam(teamQueryService, team, TeamType.REGULATOR);

    when(teamRoleRepository.existsByTeamAndWuaId(team, 1L)).thenReturn(true);

    assertThat(teamQueryService.userIsMemberOfStaticTeam(1L, TeamType.REGULATOR))
        .isTrue();
  }

  @Test
  void userIsMemberOfStaticTeam_isNotMember() {

    Team team = mock(Team.class);
    setupStaticTeam(teamQueryService, team, TeamType.REGULATOR);

    when(teamRoleRepository.existsByTeamAndWuaId(team, 1L)).thenReturn(false);

    assertThat(teamQueryService.userIsMemberOfStaticTeam(1L, TeamType.REGULATOR))
        .isFalse();
  }

  @Test
  void userIsMemberOfScopedTeam_whenMember_thenTrue() {
    var teamType = TeamType.ORGANISATION;
    var scope = TeamScopeReference.from("123", teamType);

    var team = new Team(UUID.randomUUID());
    team.setTeamType(teamType);
    team.setScopeId(scope.getId());
    team.setScopeType(teamType.getScopeType());

    TeamRole teamRole = new TeamRole();
    teamRole.setTeam(team);
    teamRole.setWuaId(1L);

    when(teamRoleRepository.findAllByWuaId(1L)).thenReturn(List.of(teamRole));

    assertThat(teamQueryService.userIsMemberOfScopedTeam(1L, teamType, scope))
        .isTrue();
  }

  @Test
  void userIsMemberOfScopedTeam_whenNotMember_thenFalse() {
    var teamType = TeamType.ORGANISATION;
    var scope = TeamScopeReference.from("123", teamType);
    
    var team = new Team(UUID.randomUUID());
    team.setTeamType(teamType);
    team.setScopeId("differentScopeId");
    team.setScopeType(teamType.getScopeType());

    TeamRole teamRole = new TeamRole();
    teamRole.setTeam(team);
    teamRole.setWuaId(1L);

    when(teamRoleRepository.findAllByWuaId(1L)).thenReturn(List.of(teamRole));

    assertThat(teamQueryService.userIsMemberOfScopedTeam(1L, teamType, scope))
        .isFalse();
  }

  @Test
  void userHasStaticRole_hasRole() {
    setupStaticTeamAndRoles(1L, TeamType.REGULATOR, List.of(
        Role.ORGANISATION_MANAGER,
        Role.TEAM_ADMINISTRATOR
    ));

    assertThat(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .isTrue();
  }

  @Test
  void userHasStaticRole_doesNotHaveRole() {
    setupStaticTeamAndRoles(1L, TeamType.REGULATOR, List.of(
        Role.ORGANISATION_MANAGER,
        Role.TEAM_ADMINISTRATOR
    ));

    assertThat(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.CONSENT_VIEWER))
        .isFalse();
  }

  @Test
  void userHasStaticRole_invalidRole() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.APPLICATION_CREATOR));
  }

  @Test
  void userHasStaticRole_noTeamInstance() {
    when(teamRepository.findByTeamType(TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.CONSENT_VIEWER))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneStaticRole_hasRole() {
    setupStaticTeamAndRoles(1L, TeamType.REGULATOR, List.of(
        Role.ORGANISATION_MANAGER,
        Role.TEAM_ADMINISTRATOR
    ));

    assertThat(teamQueryService.userHasAtLeastOneStaticRole(1L, TeamType.REGULATOR, Set.of(Role.ORGANISATION_MANAGER, Role.CONSENT_VIEWER)))
        .isTrue();
  }

  @Test
  void userHasAtLeastOneStaticRole_doesNotHaveRole() {
    setupStaticTeamAndRoles(1L, TeamType.REGULATOR, List.of(
        Role.ORGANISATION_MANAGER,
        Role.TEAM_ADMINISTRATOR
    ));

    assertThat(teamQueryService.userHasAtLeastOneStaticRole(1L, TeamType.REGULATOR, Set.of(Role.CONSENT_VIEWER)))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneStaticRole_invalidRole() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> teamQueryService.userHasAtLeastOneStaticRole(1L, TeamType.REGULATOR, Set.of(Role.APPLICATION_CREATOR)));
  }

  @Test
  void userHasAtLeastOneStaticRole_noTeamInstance() {
    when(teamRepository.findByTeamType(TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(teamQueryService.userHasAtLeastOneStaticRole(1L, TeamType.REGULATOR, Set.of(Role.ORGANISATION_MANAGER)))
        .isFalse();
  }

  @Test
  void userHasScopedRole_hasRole() {
    var scope = TeamScopeReference.from("123", "ORGGRP");
    setupScopedTeamAndRoles(1L, TeamType.ORGANISATION, scope, List.of(
        Role.TEAM_ADMINISTRATOR,
        Role.APPLICATION_SUBMITTER
    ));

    assertThat(teamQueryService.userHasScopedRole(1L, TeamType.ORGANISATION, scope, Role.APPLICATION_SUBMITTER))
        .isTrue();
  }

  @Test
  void userHasScopedRole_doesNotHaveRole() {
    var scope = TeamScopeReference.from("123", "ORGGRP");
    setupScopedTeamAndRoles(1L, TeamType.ORGANISATION, scope, List.of(
        Role.TEAM_ADMINISTRATOR,
        Role.APPLICATION_SUBMITTER
    ));

    assertThat(teamQueryService.userHasScopedRole(1L, TeamType.ORGANISATION, scope, Role.APPLICATION_CREATOR))
        .isFalse();
  }

  @Test
  void userHasScopedRole_invalidRole() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> teamQueryService.userHasScopedRole(1L, TeamType.ORGANISATION, TeamScopeReference.from("1", "ORGGRP"), Role.ORGANISATION_MANAGER));
  }

  @Test
  void userHasScopedRole_noTeamInstance() {
    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION, "ORGGRP", "1")).thenReturn(Optional.empty());
    assertThat(teamQueryService.userHasScopedRole(1L, TeamType.ORGANISATION, TeamScopeReference.from("1", "ORGGRP"), Role.APPLICATION_SUBMITTER))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneScopedRole_hasRole() {
    var scope = TeamScopeReference.from("123", "ORGGRP");
    setupScopedTeamAndRoles(1L, TeamType.ORGANISATION, scope, List.of(
        Role.TEAM_ADMINISTRATOR,
        Role.APPLICATION_SUBMITTER
    ));

    assertThat(teamQueryService.userHasAtLeastOneScopedRole(1L, TeamType.ORGANISATION, scope, Set.of(Role.APPLICATION_CREATOR, Role.APPLICATION_SUBMITTER)))
        .isTrue();
  }

  @Test
  void userHasAtLeastOneScopedRole_doesNotHaveRole() {
    var scope = TeamScopeReference.from("123", "ORGGRP");
    setupScopedTeamAndRoles(1L, TeamType.ORGANISATION, scope, List.of(
        Role.TEAM_ADMINISTRATOR,
        Role.APPLICATION_SUBMITTER
    ));

    assertThat(teamQueryService.userHasAtLeastOneScopedRole(1L, TeamType.ORGANISATION, scope, Set.of(Role.APPLICATION_CREATOR)))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneScopedRole_invalidRole() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> teamQueryService.userHasScopedRole(1L, TeamType.ORGANISATION, TeamScopeReference.from("1", "ORGGRP"), Role.CONSENT_VIEWER));
  }

  @Test
  void userHasAtLeastOneScopedRole_noTeamInstance() {
    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION, "ORGGRP", "1"))
        .thenReturn(Optional.empty());

    assertThat(teamQueryService.userHasAtLeastOneScopedRole(1L, TeamType.ORGANISATION, TeamScopeReference.from("1", "ORGGRP"), Set.of(Role.APPLICATION_SUBMITTER)))
        .isFalse();
  }

  @Test
  void userIsMemberOfAnyTeam_whenMemberOfTeam_thenTrue() {

    when(teamRoleRepository.findAllByWuaId(1L))
        .thenReturn(List.of(mock(TeamRole.class)));

    assertThat(teamQueryService.userIsMemberOfAnyTeam(1L)).isTrue();
  }

  @Test
  void userIsMemberOfAnyTeam_whenNotMemberOfTeam_thenFalse() {

    when(teamRoleRepository.findAllByWuaId(1L))
        .thenReturn(List.of());

    assertThat(teamQueryService.userIsMemberOfAnyTeam(1L)).isFalse();
  }

  @Test
  void getStaticTeamByTeamType_throwsIllegalArgumentException_whenScopedTeamType() {
    TeamType scopedTeamType = mock(TeamType.class);
    when(scopedTeamType.isScoped()).thenReturn(true);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> teamQueryService.getStaticTeamByTeamType(scopedTeamType))
        .withMessage("TeamType %s is not static".formatted(scopedTeamType));
  }

  @Test
  void getStaticTeamByTeamType_throwsIllegalStateException_whenNoTeamFound() {
    TeamType staticTeamType = mock(TeamType.class);
    when(staticTeamType.isScoped()).thenReturn(false);
    when(teamRepository.findByTeamType(staticTeamType)).thenReturn(List.of());

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> teamQueryService.getStaticTeamByTeamType(staticTeamType))
        .withMessage("No team found for static team of TeamType %s".formatted(staticTeamType));
  }

  @Test
  void getStaticTeamByTeamType_returnsTeam_whenValidStaticTeamType() {
    TeamType staticTeamType = mock(TeamType.class);
    Team expectedTeam = new Team(UUID.randomUUID());
    when(staticTeamType.isScoped()).thenReturn(false);
    when(teamRepository.findByTeamType(staticTeamType)).thenReturn(List.of(expectedTeam));

    Team actualTeam = teamQueryService.getStaticTeamByTeamType(staticTeamType);

    assertThat(actualTeam).isEqualTo(expectedTeam);
  }

  @Test
  void getTeamsOfTypeUserHasAnyRoleIn_ReturnsDistinctTeam() {
    var teamType = TeamType.ORGANISATION;
    var roles = Set.of(Role.TEAM_ADMINISTRATOR, Role.APPLICATION_SUBMITTER);

    Team team = mock(Team.class);

    TeamRole teamRole1 = new TeamRole();
    teamRole1.setTeam(team);

    TeamRole teamRole2 = new TeamRole();
    teamRole2.setTeam(team);

    when(teamRoleRepository.findByWuaIdAndTeam_TeamTypeAndRoleIn(1L, teamType, roles))
        .thenReturn(List.of(teamRole1, teamRole2));

    var result = teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(1L, teamType, roles);

    assertThat(result)
        .containsExactly(team);
  }

  @Test
  void getTeamsUserIsMemberOf_ReturnsDistinctTeams() {

    Team team1 = mock(Team.class);
    Team team2 = mock(Team.class);

    TeamRole teamRole1 = new TeamRole();
    teamRole1.setTeam(team1);

    TeamRole teamRole2 = new TeamRole();
    teamRole2.setTeam(team2);

    TeamRole teamRole3 = new TeamRole();
    teamRole3.setTeam(team2);

    when(teamRoleRepository.findAllByWuaId(1L))
        .thenReturn(List.of(teamRole1, teamRole2, teamRole3));

    var result = teamQueryService.getTeamsUserIsMemberOf(1L);

    assertThat(result)
        .containsExactly(team1, team2);
  }

  @Test
  void getRolesForUserInScopedTeams_FiltersTeamTypeAndCorrectScopeIds() {
    var teamType = TeamType.ORGANISATION;

    var scopeId1 = "1";
    var team1 = new Team();
    team1.setTeamType(teamType);
    team1.setScopeId(scopeId1);

    var scopeId2 = "2";
    var team2 = new Team();
    team2.setTeamType(teamType);
    team2.setScopeId(scopeId2);

    var team3 = new Team();
    team3.setTeamType(TeamType.CONSULTEE);
    team3.setScopeId("1");

    var teamRole1 = new TeamRole();
    teamRole1.setTeam(team1);
    teamRole1.setRole(Role.TEAM_ADMINISTRATOR);

    var teamRole2 = new TeamRole();
    teamRole2.setTeam(team2);
    teamRole2.setRole(Role.APPLICATION_SUBMITTER);

    var teamRole3 = new TeamRole();
    teamRole3.setTeam(team3);
    teamRole3.setRole(Role.RECIPIENT);

    when(teamRoleRepository.findAllByWuaId(1L))
        .thenReturn(List.of(teamRole1, teamRole3));

    var result = teamQueryService.getRolesForUserInScopedTeams(1L, teamType, Set.of(scopeId1));

    assertThat(result)
        .containsOnly(Role.TEAM_ADMINISTRATOR);
  }

  @Test
  void getMembersOfTeamTypeWithRoleIn_returnsMemberViewsForMatchingRoles() {
    var teamType = TeamType.REGULATOR;
    var roles = Set.of(Role.TEAM_ADMINISTRATOR, Role.PWA_MANAGER);

    var matchingTeamRole = new TeamRole();
    matchingTeamRole.setRole(Role.TEAM_ADMINISTRATOR);
    matchingTeamRole.setTeam(new Team());

    var nonMatchingTeamRole = new TeamRole();
    nonMatchingTeamRole.setRole(Role.CONSENT_VIEWER);
    nonMatchingTeamRole.setTeam(new Team());

    when(teamRoleRepository.findAllByTeam_TeamType(teamType))
        .thenReturn(List.of(matchingTeamRole, nonMatchingTeamRole));

    var expectedViews = List.of(mock(TeamMemberView.class));
    when(teamMemberQueryService.getTeamMemberViewsByTeamRoles(List.of(matchingTeamRole)))
        .thenReturn(expectedViews);

    var result = teamQueryService.getMembersOfTeamTypeWithRoleIn(teamType, roles);

    assertThat(result).isEqualTo(expectedViews);
  }

  @Test
  void getMembersOfScopedTeamWithRoleIn_returnsMemberViewsForMatchingScopeAndRoles() {
    var teamType = TeamType.CONSULTEE;
    var scopeRef = TeamScopeReference.from("scope123", teamType);
    var roles = Set.of(Role.RECIPIENT, Role.RESPONDER);

    var matchingTeam = new Team();
    matchingTeam.setScopeId("scope123");
    matchingTeam.setScopeType(teamType.getScopeType());

    var nonMatchingTeam = new Team();
    nonMatchingTeam.setScopeId("otherScope");
    nonMatchingTeam.setScopeType(teamType.getScopeType());

    var matchingTeamRole = new TeamRole();
    matchingTeamRole.setTeam(matchingTeam);
    matchingTeamRole.setRole(Role.RECIPIENT);

    var nonMatchingRoleTeamRole = new TeamRole();
    nonMatchingRoleTeamRole.setTeam(matchingTeam);
    nonMatchingRoleTeamRole.setRole(Role.TEAM_ADMINISTRATOR);

    var nonMatchingScopeTeamRole = new TeamRole();
    nonMatchingScopeTeamRole.setTeam(nonMatchingTeam);
    nonMatchingScopeTeamRole.setRole(Role.RECIPIENT);

    when(teamRoleRepository.findAllByTeam_TeamType(teamType))
        .thenReturn(List.of(matchingTeamRole, nonMatchingRoleTeamRole, nonMatchingScopeTeamRole));

    var expectedViews = List.of(mock(TeamMemberView.class));
    when(teamMemberQueryService.getTeamMemberViewsByTeamRoles(List.of(matchingTeamRole)))
        .thenReturn(expectedViews);

    var result = teamQueryService.getMembersOfScopedTeamWithRoleIn(teamType, scopeRef, roles);

    assertThat(result).isEqualTo(expectedViews);
  }


  private void setupStaticTeamAndRoles(Long wuaId, TeamType teamType, List<Role> roles) {
    var team = new Team(UUID.randomUUID());
    team.setTeamType(teamType);
    var teamRoles = roles.stream()
        .map(role -> createTeamRole(wuaId, team, role))
        .toList();

    when(teamRepository.findByTeamType(teamType))
        .thenReturn(List.of(team));
    when(teamRoleRepository.findByWuaIdAndTeam(wuaId, team))
        .thenReturn(teamRoles);
  }

  private void setupScopedTeamAndRoles(Long wuaId, TeamType teamType, TeamScopeReference scopeRef, List<Role> roles) {
    var team = new Team(UUID.randomUUID());
    team.setScopeType(scopeRef.getType());
    team.setScopeId(scopeRef.getId());
    team.setTeamType(teamType);
    var teamRoles = roles.stream()
        .map(role -> createTeamRole(wuaId, team, role))
        .toList();

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId()))
        .thenReturn(Optional.of(team));
    when(teamRoleRepository.findByWuaIdAndTeam(wuaId, team))
        .thenReturn(teamRoles);
  }


  private TeamRole createTeamRole(Long wuaId, Team team, Role role) {
    var teamRole = new TeamRole(UUID.randomUUID());
    teamRole.setWuaId(wuaId);
    teamRole.setTeam(team);
    teamRole.setRole(role);
    return teamRole;
  }

  private void setupStaticTeam(TeamQueryService spy, Team team, TeamType teamType) {
    doReturn(team).when(spy).getStaticTeamByTeamType(teamType);
  }

}
