package uk.co.ogauthority.pwa.teams.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.UsersProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamRepository;
import uk.co.ogauthority.pwa.teams.TeamRole;
import uk.co.ogauthority.pwa.teams.TeamRoleRepository;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

@ExtendWith(MockitoExtension.class)
class TeamManagementServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamRoleRepository teamRoleRepository;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private UserApi userApi;

  @InjectMocks
  private TeamManagementService teamManagementService;

  @Captor
  private ArgumentCaptor<Team> teamArgumentCaptor;
  @Captor
  private ArgumentCaptor<List<TeamRole>> teamRoleListCaptor;

  private static Team regTeam;
  private static Team orgTeam1;
  private static Team orgTeam2;

  private static TeamRole regTeamUser1RoleManage;
  private static TeamRole regTeamUser1RoleOrgAdmin;
  private static TeamRole regTeamUser2RoleOrgAdmin;

  private static TeamRole orgTeam1User1RoleManage;
  private static TeamRole orgTeam2User1RoleManage;

  private static final Long user1WuaId = 1L;
  private static User user1;
  private static final Long user2WuaId = 2L;
  private static User user2;

  @BeforeAll
  public static void setUp() {
    regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeamUser1RoleManage = new TeamRole();
    regTeamUser1RoleManage.setTeam(regTeam);
    regTeamUser1RoleManage.setWuaId(user1WuaId);
    regTeamUser1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    regTeamUser1RoleOrgAdmin = new TeamRole();
    regTeamUser1RoleOrgAdmin.setTeam(regTeam);
    regTeamUser1RoleOrgAdmin.setWuaId(user1WuaId);
    regTeamUser1RoleOrgAdmin.setRole(Role.ORGANISATION_MANAGER);

    regTeamUser2RoleOrgAdmin = new TeamRole();
    regTeamUser2RoleOrgAdmin.setTeam(regTeam);
    regTeamUser2RoleOrgAdmin.setWuaId(user2WuaId);
    regTeamUser2RoleOrgAdmin.setRole(Role.ORGANISATION_MANAGER);

    orgTeam1 = new Team(UUID.randomUUID());
    orgTeam1.setTeamType(TeamType.ORGANISATION);
    orgTeam1User1RoleManage = new TeamRole();
    orgTeam1User1RoleManage.setTeam(orgTeam1);
    orgTeam1User1RoleManage.setWuaId(user1WuaId);
    orgTeam1User1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    orgTeam2 = new Team(UUID.randomUUID());
    orgTeam2.setTeamType(TeamType.ORGANISATION);
    orgTeam2User1RoleManage = new TeamRole();
    orgTeam2User1RoleManage.setTeam(orgTeam2);
    orgTeam2User1RoleManage.setWuaId(user1WuaId);
    orgTeam2User1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    user1 = new User();
    user1.setWebUserAccountId(Math.toIntExact(user1WuaId));
    user1.setTitle("Ms");
    user1.setForename("User");
    user1.setSurname("One");
    user1.setPrimaryEmailAddress("one@example.com");
    user1.setTelephoneNumber("1");
    user1.setCanLogin(true);
    user1.setIsAccountShared(false);

    user2 = new User();
    user2.setWebUserAccountId(Math.toIntExact(user2WuaId));
    user2.setTitle("Mr");
    user2.setForename("User");
    user2.setSurname("Two");
    user2.setPrimaryEmailAddress("two@example.com");
    user2.setTelephoneNumber("2");
    user2.setCanLogin(true);
    user2.setIsAccountShared(false);

  }

  @Test
  void createScopedTeam() {
    var scopeRef = TeamScopeReference.from("1", "ORGGRP");

    teamManagementService.createScopedTeam("foo", TeamType.ORGANISATION, scopeRef);

    verify(teamRepository).save(teamArgumentCaptor.capture());
    var newTeam = teamArgumentCaptor.getValue();

    assertThat(newTeam.getName()).isEqualTo("foo");
    assertThat(newTeam.getTeamType()).isEqualTo(TeamType.ORGANISATION);
    assertThat(newTeam.getScopeType()).isEqualTo(scopeRef.getType());
    assertThat(newTeam.getScopeId()).isEqualTo(scopeRef.getId());
  }

  @Test
  void createScopedTeam_wrongType() {
    var scopeRef = TeamScopeReference.from("1", "ORGGRP");

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.createScopedTeam("foo", TeamType.REGULATOR, scopeRef));
    verify(teamRepository, never()).save(any());
  }

  @Test
  void createScopedTeam_alreadyExists() {
    var scopeRef = TeamScopeReference.from("1", "ORGGRP");

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION, "ORGGRP", "1"))
        .thenReturn(Optional.of(orgTeam1));

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.createScopedTeam("foo", TeamType.ORGANISATION, scopeRef));

    verify(teamRepository, never()).save(any());
  }

  @Test
  void getTeamTypesUserIsMemberOf() {

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage, orgTeam2User1RoleManage));

    assertThat(teamManagementService.getTeamTypesUserIsMemberOf(user1WuaId))
        .containsExactlyInAnyOrder(TeamType.REGULATOR, TeamType.ORGANISATION);
  }

  @Test
  void getStaticTeamOfTypeUserCanManage() {
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage, orgTeam2User1RoleManage));

    assertThat(teamManagementService.getStaticTeamOfTypeUserCanManage(TeamType.REGULATOR, user1WuaId))
        .hasValue(regTeam);
  }

  @Test
  void getStaticTeamOfTypeUserCanManage_notStatic() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.getStaticTeamOfTypeUserCanManage(TeamType.ORGANISATION, user1WuaId));
  }

  @Test
  void getScopedTeamOfTypeUserCanManage() {
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage));

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION, user1WuaId))
        .containsExactlyInAnyOrder(orgTeam1);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_regulatorWithRoleCanManageAllOrgs() {
    // User has direct manage team role in reg team and org team 1
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage));

    // User has the special create/manage any org team priv
    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    // There are 2 org teams
    when(teamRepository.findByTeamType(TeamType.ORGANISATION))
        .thenReturn(List.of(orgTeam1, orgTeam2));

    // Verify they can manage both org team 1 and 2
    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION, user1WuaId))
        .containsExactlyInAnyOrder(orgTeam1, orgTeam2);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_notScoped() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.REGULATOR, user1WuaId));
  }

  @Test
  void getTeam() {
    var uuid = UUID.randomUUID();
    when(teamRepository.findById(uuid))
        .thenReturn(Optional.of(regTeam));

    assertThat(teamManagementService.getTeam(uuid))
        .isEqualTo(Optional.of(regTeam));
  }

  @Test
  void getEnergyPortalUser() {
    var expectedProjection = new UsersProjectionRoot()
        .webUserAccountId()
        .isAccountShared()
        .canLogin();

    teamManagementService.getEnergyPortalUser("foo");

    verify(userApi).searchUsersByEmail(eq("foo"), refEq(expectedProjection), any(RequestPurpose.class));
  }

  @Test
  void getTeamMemberView() {
    when(teamRoleRepository.findByWuaIdAndTeam(user1WuaId, regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage, regTeamUser1RoleOrgAdmin));

    var expectedProjection = new UserProjectionRoot()
        .webUserAccountId()
        .title()
        .forename()
        .surname()
        .primaryEmailAddress()
        .telephoneNumber();

    var user = new User();
    user.setWebUserAccountId(1);
    user.setTitle("Ms");
    user.setForename("Foo");
    user.setSurname("Bar");
    user.setPrimaryEmailAddress("text@example.com");
    user.setTelephoneNumber("012345678");
    user.setCanLogin(true);
    user.setIsAccountShared(false);

    when(userApi.findUserById(eq(1), refEq(expectedProjection), any(RequestPurpose.class)))
        .thenReturn(Optional.of(user));

    var teamMemberView = teamManagementService.getTeamMemberView(regTeam, user1WuaId);

    assertThat(teamMemberView.wuaId()).isEqualTo(Long.valueOf(user.getWebUserAccountId()));
    assertThat(teamMemberView.title()).isEqualTo(user.getTitle());
    assertThat(teamMemberView.forename()).isEqualTo(user.getForename());
    assertThat(teamMemberView.surname()).isEqualTo(user.getSurname());
    assertThat(teamMemberView.email()).isEqualTo(user.getPrimaryEmailAddress());
    assertThat(teamMemberView.telNo()).isEqualTo(user.getTelephoneNumber());
    assertThat(teamMemberView.teamId()).isEqualTo(regTeam.getId());
    assertThat(teamMemberView.roles()).containsExactlyInAnyOrder(regTeamUser1RoleManage.getRole(), regTeamUser1RoleOrgAdmin.getRole());
  }

  @Test
  void getTeamMemberViewsForTeam() {

    // the list returns roles not in the order declared in the TeamType enum
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleOrgAdmin, regTeamUser1RoleManage, regTeamUser2RoleOrgAdmin));

    var expectedProjection = new UsersProjectionRoot()
        .webUserAccountId()
        .title()
        .forename()
        .surname()
        .primaryEmailAddress()
        .telephoneNumber();

    when(userApi.searchUsersByIds(eq(List.of(1,2)), refEq(expectedProjection), any(RequestPurpose.class)))
        .thenReturn(List.of(user1, user2));

    var teamMemberViews = teamManagementService.getTeamMemberViewsForTeam(regTeam);

    assertThat(teamMemberViews)
        .extracting(
            TeamMemberView::wuaId,
            TeamMemberView::title,
            TeamMemberView::forename,
            TeamMemberView::surname,
            TeamMemberView::email,
            TeamMemberView::telNo,
            TeamMemberView::teamId,
            TeamMemberView::roles
        )
        .containsExactly(
            tuple(
                Long.valueOf(user1.getWebUserAccountId()),
                user1.getTitle(),
                user1.getForename(),
                user1.getSurname(),
                user1.getPrimaryEmailAddress(),
                user1.getTelephoneNumber(),
                regTeam.getId(),
                List.of(regTeamUser1RoleManage.getRole(), regTeamUser1RoleOrgAdmin.getRole())
            ),
            tuple(
                Long.valueOf(user2.getWebUserAccountId()),
                user2.getTitle(),
                user2.getForename(),
                user2.getSurname(),
                user2.getPrimaryEmailAddress(),
                user2.getTelephoneNumber(),
                regTeam.getId(),
                List.of(regTeamUser2RoleOrgAdmin.getRole())
            )
        );
  }

  @Test
  void setUserTeamRoles() {
    var expectedProjection = new UserProjectionRoot()
        .isAccountShared()
        .canLogin();
    when(userApi.findUserById(eq(1), refEq(expectedProjection), any(RequestPurpose.class)))
        .thenReturn(Optional.of(user1));
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage)); // Make doesTeamHaveTeamManager() check return true

    teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER));

    verify(teamRoleRepository).deleteByWuaIdAndTeam(user1WuaId, regTeam);
    verify(teamRoleRepository).saveAll(teamRoleListCaptor.capture());

    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getTeam)
        .contains(regTeam, regTeam);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getWuaId)
        .contains(user1WuaId, user1WuaId);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getRole)
        .contains(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER);
  }

  @Test
  void setUserTeamRoles_noTeamManagerLeft() {
    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.of(user1));

    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of()); // Make doesTeamHaveTeamManager() check return false

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.ORGANISATION_MANAGER)));
  }

  @Test
  void setUserTeamRoles_invalidRoles() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.APPLICATION_CREATOR)));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_noEpaUser() {
    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER)));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_sharedAccount() {
    var epaUser = new User();
    epaUser.setIsAccountShared(true);

    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER)));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_canNotLogin() {
    var epaUser = new User();
    epaUser.setCanLogin(false);

    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER)));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void removeUserFromTeam() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    teamManagementService.removeUserFromTeam(user2WuaId, regTeam);
    verify(teamRoleRepository).deleteByWuaIdAndTeam(user2WuaId, regTeam);
  }

  @Test
  void removeUserFromTeam_lastTeamManager() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.removeUserFromTeam(user1WuaId, regTeam));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(user1WuaId, regTeam);
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, user2WuaId, List.of(Role.ORGANISATION_MANAGER)))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate_newRolesIncludeManage() {
    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, user1WuaId, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER)))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate_noManageRoleLeft() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, user1WuaId, List.of(Role.ORGANISATION_MANAGER)))
        .isFalse();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRemoval() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, user2WuaId))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRemoval_noManageRoleLeft() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, user1WuaId))
        .isFalse();
  }

  @Test
  void doesScopedTeamWithReferenceExist_existingTeam() {
    var scopeRef = TeamScopeReference.from("1", "ORGGRP");

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION, "ORGGRP", "1"))
        .thenReturn(Optional.of(orgTeam1));

    assertThat(teamManagementService.doesScopedTeamWithReferenceExist(TeamType.ORGANISATION, scopeRef))
        .isTrue();
  }

  @Test
  void doesScopedTeamWithReferenceExist_noExistingTeam() {
    var scopeRef = TeamScopeReference.from("1", "ORGGRP");

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION, "ORGGRP", "1"))
        .thenReturn(Optional.empty());

    assertThat(teamManagementService.doesScopedTeamWithReferenceExist(TeamType.ORGANISATION, scopeRef))
        .isFalse();
  }

  @Test
  void userCanManageAnyOrganisationTeam_whenHasRole_thenTrue() {

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    assertThat(teamManagementService.userCanManageAnyOrganisationTeam(user1WuaId)).isTrue();
  }

  @Test
  void userCanManageAnyOrganisationTeam_whenNoRole_thenFalse() {

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.userCanManageAnyOrganisationTeam(user1WuaId)).isFalse();
  }

  @Test
  void isMemberOfTeam_whenMemberOfTeam_thenTrue() {

    when(teamRoleRepository.existsByTeamAndWuaId(regTeam, user1WuaId))
        .thenReturn(true);

    assertThat(teamManagementService.isMemberOfTeam(regTeam, user1WuaId)).isTrue();
  }

  @Test
  void isMemberOfTeam_whenNotMemberOfTeam_thenFalse() {

    when(teamRoleRepository.existsByTeamAndWuaId(regTeam, user1WuaId))
        .thenReturn(false);

    assertThat(teamManagementService.isMemberOfTeam(regTeam, user1WuaId)).isFalse();
  }

  @Test
  void canManageTeam_whenScopedTeam_andCanManageTeam_thenTrue() {

    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION);

    var teamRole = new TeamRole();
    teamRole.setTeam(scopedTeam);
    teamRole.setRole(Role.TEAM_ADMINISTRATOR);

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(teamRole));

    assertThat(teamManagementService.canManageTeam(scopedTeam, user1WuaId)).isTrue();
  }

  @Test
  void canManageTeam_whenScopedTeam_andCannotManageTeam_thenFalse() {

    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION);

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of());

    assertThat(teamManagementService.canManageTeam(scopedTeam, user1WuaId)).isFalse();
  }

  @Test
  void canManageTeam_whenOrganisationScopedTeam_andCannotManageTeam_andHasManageAnyOrganisationTeamRole_thenTrue() {

    // GIVEN a scoped organisation team
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION);

    // AND the user doesn't have the manage team permission in that team
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of());

    // WHEN the user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM role in the regulator team
    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(TeamType.ORGANISATION))
        .thenReturn(List.of(scopedTeam));

    // THEN the user can manage the team
    assertThat(teamManagementService.canManageTeam(scopedTeam, user1WuaId)).isTrue();
  }

  @Test
  void canManageTeam_whenStaticTeam_andCanManageTeam_thenTrue() {

    var staticTeam = new Team((UUID.randomUUID()));
    staticTeam.setTeamType(TeamType.REGULATOR);

    var teamRole = new TeamRole();
    teamRole.setTeam(staticTeam);
    teamRole.setRole(Role.TEAM_ADMINISTRATOR);

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(teamRole));

    assertThat(teamManagementService.canManageTeam(staticTeam, user1WuaId)).isTrue();
  }

  @Test
  void canManageTeam_whenStaticTeam_andCannotManageTeam_thenFalse() {

    var staticTeam = new Team((UUID.randomUUID()));
    staticTeam.setTeamType(TeamType.REGULATOR);

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of());

    assertThat(teamManagementService.canManageTeam(staticTeam, user1WuaId)).isFalse();
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenScopedTeamType_thenException() {

    var scopedTeamType = TeamType.ORGANISATION;

    assertThatThrownBy(() -> teamManagementService.getStaticTeamOfTypeUserIsMemberOf(scopedTeamType, user1WuaId))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenNotMemberOfTeamOfType_thenEmptyOptional() {

    var staticTeamType = TeamType.REGULATOR;

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of());

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, user1WuaId);

    assertThat(resultingTeam).isEmpty();
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenMemberOfTeamOfType_thenTeamReturned() {

    var staticTeamType = TeamType.REGULATOR;

    var expectedTeam = new Team(UUID.randomUUID());
    expectedTeam.setTeamType(staticTeamType);

    var teamRole = new TeamRole();
    teamRole.setTeam(expectedTeam);

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of(teamRole));

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, user1WuaId);

    assertThat(resultingTeam).contains(expectedTeam);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenStaticTeamType_thenException() {

    var staticTeamType = TeamType.REGULATOR;

    assertThatThrownBy(() -> teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(staticTeamType, user1WuaId))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserNotMemberOfAnyTeamOfType_thenEmptySetReturned() {

    var scopedTeamType = TeamType.ORGANISATION;

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of());

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, user1WuaId);

    assertThat(resultingScopedTeams).isEmpty();
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserMemberOfTeamOfType_thenScopedTeamsReturned() {

    var scopedTeamType = TeamType.ORGANISATION;

    var firstTeamOfType = new Team(UUID.randomUUID());
    firstTeamOfType.setTeamType(scopedTeamType);

    var firstRoleForFirstTeam = new TeamRole();
    firstRoleForFirstTeam.setTeam(firstTeamOfType);
    firstRoleForFirstTeam.setRole(Role.TEAM_ADMINISTRATOR);

    var secondRoleForFirstTeam = new TeamRole();
    secondRoleForFirstTeam.setTeam(firstTeamOfType);
    secondRoleForFirstTeam.setRole(Role.ORGANISATION_MANAGER);

    var secondTeamOfType = new Team(UUID.randomUUID());
    secondTeamOfType.setTeamType(scopedTeamType);

    var firstRoleForSecondTeam = new TeamRole();
    firstRoleForSecondTeam.setTeam(secondTeamOfType);
    firstRoleForSecondTeam.setRole(Role.TEAM_ADMINISTRATOR);

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of(firstRoleForSecondTeam, firstRoleForFirstTeam, secondRoleForFirstTeam));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, user1WuaId);

    assertThat(resultingScopedTeams)
        .containsExactlyInAnyOrder(firstTeamOfType, secondTeamOfType);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserHasManageAnyOrganisationTeamRole_thenAllOrganisationTeamsReturned() {

    var scopedTeamType = TeamType.ORGANISATION;

    var teamUserIsMemberOf = new Team(UUID.randomUUID());
    teamUserIsMemberOf.setTeamType(scopedTeamType);

    var roleForTeamUserIsMemberOf = new TeamRole();
    roleForTeamUserIsMemberOf.setTeam(teamUserIsMemberOf);
    roleForTeamUserIsMemberOf.setRole(Role.TEAM_ADMINISTRATOR);

    var teamUserIsNotMemberOf = new Team(UUID.randomUUID());
    teamUserIsNotMemberOf.setTeamType(scopedTeamType);

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of(roleForTeamUserIsMemberOf));

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(scopedTeamType))
        .thenReturn(List.of(teamUserIsNotMemberOf, teamUserIsMemberOf));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, user1WuaId);

    assertThat(resultingScopedTeams)
        .containsExactlyInAnyOrder(teamUserIsNotMemberOf, teamUserIsMemberOf);
  }
}