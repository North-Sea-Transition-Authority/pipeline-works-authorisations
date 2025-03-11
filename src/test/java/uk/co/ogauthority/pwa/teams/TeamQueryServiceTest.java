package uk.co.ogauthority.pwa.teams;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamQueryServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamRoleRepository teamRoleRepository;

  @InjectMocks
  private TeamQueryService teamQueryService;

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

}
