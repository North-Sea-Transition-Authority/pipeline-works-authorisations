package uk.co.ogauthority.pwa.teams.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.EnergyPortalAccessService;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.InstigatingWebUserAccountId;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.ResourceType;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.TargetWebUserAccountId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.UsersProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.ogauthority.pwa.integrations.energyportal.access.EnergyPortalAccessApiConfiguration;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamMemberQueryService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamRepository;
import uk.co.ogauthority.pwa.teams.TeamRole;
import uk.co.ogauthority.pwa.teams.TeamRoleRepository;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;

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

  @Mock
  private EnergyPortalAccessService energyPortalAccessService;

  @Mock
  private EnergyPortalAccessApiConfiguration energyPortalAccessApiConfiguration;

  @Mock
  private TeamMemberQueryService teamMemberQueryService;

  @Spy
  @InjectMocks
  private TeamManagementService teamManagementService;

  @Captor
  private ArgumentCaptor<Team> teamArgumentCaptor;
  @Captor
  private ArgumentCaptor<List<TeamRole>> teamRoleListCaptor;
  @Captor
  private ArgumentCaptor<ResourceType> resourceTypeArgumentCaptor;
  @Captor
  private ArgumentCaptor<TargetWebUserAccountId> targetWebUserAccountIdArgumentCaptor;
  @Captor
  private ArgumentCaptor<InstigatingWebUserAccountId> instigatingWebUserAccountIdArgumentCaptor;

  private static Team regTeam;
  private static Team orgTeam1;
  private static Team orgTeam2;
  private static Team cgTeam1;
  private static Team cgTeam2;

  private static TeamRole regTeamUser1RoleManage;

  private static TeamRole orgTeam1User1RoleManage;
  private static TeamRole orgTeam2User1RoleManage;

  private static TeamRole cgTeam1User1RoleManage;
  private static TeamRole cgTeam2User1RoleManage;

  private static final Long user1WuaId = 1L;
  private static User user1;
  private static final Long user2WuaId = 2L;

  @BeforeAll
  static void setUp() {
    regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeamUser1RoleManage = new TeamRole();
    regTeamUser1RoleManage.setTeam(regTeam);
    regTeamUser1RoleManage.setWuaId(user1WuaId);
    regTeamUser1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

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

    cgTeam1 = new Team(UUID.randomUUID());
    cgTeam1.setTeamType(TeamType.CONSULTEE);
    cgTeam1User1RoleManage = new TeamRole();
    cgTeam1User1RoleManage.setTeam(cgTeam1);
    cgTeam1User1RoleManage.setWuaId(user1WuaId);
    cgTeam1User1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    cgTeam2 = new Team(UUID.randomUUID());
    cgTeam2.setTeamType(TeamType.CONSULTEE);
    cgTeam2User1RoleManage = new TeamRole();
    cgTeam2User1RoleManage.setTeam(cgTeam2);
    cgTeam2User1RoleManage.setWuaId(user1WuaId);
    cgTeam2User1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    user1 = new User();
    user1.setWebUserAccountId(Math.toIntExact(user1WuaId));
    user1.setTitle("Ms");
    user1.setForename("User");
    user1.setSurname("One");
    user1.setPrimaryEmailAddress("one@example.com");
    user1.setTelephoneNumber("1");
    user1.setCanLogin(true);
    user1.setIsAccountShared(false);

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
  void getScopedTeamOfTypeUserCanManage_OrgTeam() {
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage));

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION, user1WuaId))
        .containsExactlyInAnyOrder(orgTeam1);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_ConsulteeGroupTeam() {
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, cgTeam1User1RoleManage));

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.CONSULTEE, user1WuaId))
        .containsExactlyInAnyOrder(cgTeam1);
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
  void getScopedTeamOfTypeUserCanManage_regulatorWithRoleCanManageAllConsulteeGroups() {
    // User has direct manage team role in reg team and org team 1
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, cgTeam1User1RoleManage));

    // User has the special create/manage any org team priv
    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(true);

    // There are 2 cg teams
    when(teamRepository.findByTeamType(TeamType.CONSULTEE))
        .thenReturn(List.of(cgTeam1, cgTeam2));

    // Verify they can manage both org team 1 and 2
    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.CONSULTEE, user1WuaId))
        .containsExactlyInAnyOrder(cgTeam1, cgTeam2);
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
  void setUserTeamRoles_existingUser() {
    var expectedProjection = new UserProjectionRoot()
        .isAccountShared()
        .canLogin();
    when(userApi.findUserById(eq(1), refEq(expectedProjection), any(RequestPurpose.class)))
        .thenReturn(Optional.of(user1));
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage)); // Make doesTeamHaveTeamManager() check return true
    when(teamRoleRepository.findAllByWuaId(1)).thenReturn(List.of(new TeamRole()));

    long instigatingUser = 2;
    teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER), instigatingUser);

    verify(teamRoleRepository).deleteByWuaIdAndTeam(user1WuaId, regTeam);
    verify(teamRoleRepository).saveAll(teamRoleListCaptor.capture());
    verifyNoInteractions(energyPortalAccessService);

    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getTeam)
        .contains(regTeam, regTeam);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getWuaId)
        .contains(user1WuaId, user1WuaId);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getRole)
        .contains(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER);
  }

  @Test
  void setUserTeamRoles_newUser() {
    var expectedProjection = new UserProjectionRoot()
        .isAccountShared()
        .canLogin();
    when(userApi.findUserById(eq(1), refEq(expectedProjection), any(RequestPurpose.class)))
        .thenReturn(Optional.of(user1));
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage)); // Make doesTeamHaveTeamManager() check return true
    when(teamRoleRepository.findAllByWuaId(1)).thenReturn(Collections.emptyList());
    var resourceTypeName = "REMI_ACCESS_TEAM";
    when(energyPortalAccessApiConfiguration.resourceType()).thenReturn(resourceTypeName);

    long instigatingUser = 2;
    teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER), instigatingUser);

    verify(teamRoleRepository).deleteByWuaIdAndTeam(user1WuaId, regTeam);
    verify(teamRoleRepository).saveAll(teamRoleListCaptor.capture());
    verify(energyPortalAccessService).addUserToAccessTeam(
        resourceTypeArgumentCaptor.capture(),
        targetWebUserAccountIdArgumentCaptor.capture(),
        instigatingWebUserAccountIdArgumentCaptor.capture()
    );

    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getTeam)
        .contains(regTeam, regTeam);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getWuaId)
        .contains(user1WuaId, user1WuaId);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getRole)
        .contains(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER);
    assertThat(resourceTypeArgumentCaptor.getValue().name()).isEqualTo(resourceTypeName);
    assertThat(targetWebUserAccountIdArgumentCaptor.getValue()).extracting(TargetWebUserAccountId::getId).isEqualTo(user1WuaId);
    assertThat(instigatingWebUserAccountIdArgumentCaptor.getValue()).extracting(InstigatingWebUserAccountId::getId).isEqualTo(instigatingUser);
  }

  @Test
  void setUserTeamRoles_noTeamManagerLeft() {
    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.of(user1));

    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of()); // Make doesTeamHaveTeamManager() check return false

    long instigatingUser = 2;
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.ORGANISATION_MANAGER), instigatingUser));
  }

  @Test
  void setUserTeamRoles_invalidRoles() {
    long instigatingUser = 2;
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.APPLICATION_CREATOR), instigatingUser));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_noEpaUser() {
    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    long instigatingUser = 2;
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER), instigatingUser));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_sharedAccount() {
    var epaUser = new User();
    epaUser.setIsAccountShared(true);

    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    long instigatingUser = 2;
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER), instigatingUser));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_canNotLogin() {
    var epaUser = new User();
    epaUser.setCanLogin(false);

    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    long instigatingUser = 2;
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER), instigatingUser));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void removeUserFromTeam_inNoMoreTeams() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));
    when(teamRoleRepository.findAllByWuaId(user2WuaId)).thenReturn(Collections.emptyList());
    var resourceTypeName = "PWA_ACCESS";
    when(energyPortalAccessApiConfiguration.resourceType()).thenReturn(resourceTypeName);

    teamManagementService.removeUserFromTeam(user2WuaId, regTeam);
    verify(teamRoleRepository).deleteByWuaIdAndTeam(user2WuaId, regTeam);
    verify(energyPortalAccessService).removeUserFromAccessTeam(
        resourceTypeArgumentCaptor.capture(),
        targetWebUserAccountIdArgumentCaptor.capture(),
        instigatingWebUserAccountIdArgumentCaptor.capture()
    );

    assertThat(resourceTypeArgumentCaptor.getValue().name()).isEqualTo(resourceTypeName);
    assertThat(targetWebUserAccountIdArgumentCaptor.getValue()).extracting(TargetWebUserAccountId::getId).isEqualTo(user2WuaId);
  }

  @Test
  void removeUserFromTeam_stillInTeams() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));
    var teamRole = new TeamRole();
    when(teamRoleRepository.findAllByWuaId(user2WuaId)).thenReturn(List.of(teamRole));

    teamManagementService.removeUserFromTeam(user2WuaId, regTeam);
    verify(teamRoleRepository).deleteByWuaIdAndTeam(user2WuaId, regTeam);
    verifyNoInteractions(energyPortalAccessService);
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
  void canAddUserToTeam_staticTeamType_alwaysTrue() {
    var staticTeam = new Team(UUID.randomUUID());
    staticTeam.setTeamType(TeamType.REGULATOR); // not a scoped team

    boolean result = teamManagementService.canAddUserToTeam(user1WuaId, staticTeam);

    assertThat(result).isTrue();
  }

  @Test
  void canAddUserToTeam_scopedTeamType_singleTeamUserNotMember_returnsTrue() {
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.CONSULTEE); // scoped type with SINGLE_TEAM restriction

    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeam.getTeamType(), user1WuaId))
        .thenReturn(Collections.emptySet());

    boolean result = teamManagementService.canAddUserToTeam(user1WuaId, scopedTeam);

    assertThat(result).isTrue();
  }

  @Test
  void canAddUserToTeam_scopedTeamType_singleTeamUserIsMember_returnsFalse() {
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.CONSULTEE); // scoped type with SINGLE_TEAM restriction

    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeam.getTeamType(), user1WuaId))
        .thenReturn(Set.of(new Team()));

    boolean result = teamManagementService.canAddUserToTeam(user1WuaId, scopedTeam);

    assertThat(result).isFalse();
  }

  @Test
  void canAddUserToTeam_scopedTeamType_multipleTeamsAlwaysTrue() {
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION); // scoped type with MULTIPLE_TEAMS restriction

    boolean result = teamManagementService.canAddUserToTeam(user1WuaId, scopedTeam);

    assertThat(result).isTrue();
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
  void userCanManageAnyConsulteeGroupTeam_whenHasRole_thenTrue() {

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(true);

    assertThat(teamManagementService.userCanManageAnyConsulteeGroupTeam(user1WuaId)).isTrue();
  }

  @Test
  void userCanManageAnyConsulteeGroupTeam_whenNoRole_thenFalse() {

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.userCanManageAnyConsulteeGroupTeam(user1WuaId)).isFalse();
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
  void canManageTeam_whenConsulteeGroupScopedTeam_andCannotManageTeam_andHasManageAnyConsulteeGroupTeamRole_thenTrue() {

    // GIVEN a scoped organisation team
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.CONSULTEE);

    // AND the user doesn't have the manage team permission in that team
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of());

    // WHEN the user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM role in the regulator team
    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(TeamType.CONSULTEE))
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

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserHasManageAnyConsulteeGroupTeamRole_thenAllConsulteeGroupTeamsReturned() {

    var scopedTeamType = TeamType.CONSULTEE;

    var teamUserIsMemberOf = new Team(UUID.randomUUID());
    teamUserIsMemberOf.setTeamType(scopedTeamType);

    var roleForTeamUserIsMemberOf = new TeamRole();
    roleForTeamUserIsMemberOf.setTeam(teamUserIsMemberOf);
    roleForTeamUserIsMemberOf.setRole(Role.TEAM_ADMINISTRATOR);

    var teamUserIsNotMemberOf = new Team(UUID.randomUUID());
    teamUserIsNotMemberOf.setTeamType(scopedTeamType);

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of(roleForTeamUserIsMemberOf));

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(scopedTeamType))
        .thenReturn(List.of(teamUserIsNotMemberOf, teamUserIsMemberOf));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, user1WuaId);

    assertThat(resultingScopedTeams)
        .containsExactlyInAnyOrder(teamUserIsNotMemberOf, teamUserIsMemberOf);
  }
}