package uk.co.ogauthority.pwa.teams.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamDto;
import uk.co.fivium.energyportal.starter.accounts.EnergyPortalServiceAccessService;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalAccountsMessagePublishingService;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.UsersProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.ogauthority.pwa.config.ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamMemberQueryService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamRepository;
import uk.co.ogauthority.pwa.teams.TeamRole;
import uk.co.ogauthority.pwa.teams.TeamRoleRepository;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamTestUtil;
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
  private TeamMemberQueryService teamMemberQueryService;

  @Mock
  private PwaContactRepository pwaContactRepository;

  @Mock
  private UserAccountService userAccountService;

  @Mock
  private EnergyPortalServiceAccessService energyPortalServiceAccessService;

  @Mock
  private EnergyPortalAccountsMessagePublishingService energyPortalAccountsMessagePublishingService;

  private TeamManagementService teamManagementService;

  @Captor
  private ArgumentCaptor<Team> teamArgumentCaptor;
  @Captor
  private ArgumentCaptor<List<TeamRole>> teamRoleListCaptor;

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

  private static final Long USER_1_WUA_ID = 1L;
  private static User user1;
  private static final Long USER_2_WUA_ID = 2L;

  private static final ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties configProperties
      = new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties(Map.of(
      1, new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties.ScopeTypeAndIdDto(
          ScopeType.ORGANISATION_GROUP,
          111
      ),
      2, new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties.ScopeTypeAndIdDto(
          ScopeType.ORGANISATION_UNIT,
          222
      ),
      3, new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties.ScopeTypeAndIdDto(
          ScopeType.ORGANISATION_UNIT,
          133311
      )
  ));

  @BeforeEach
  void setUp() {
    teamManagementService = Mockito.spy(new TeamManagementService(
        teamRepository,
        teamRoleRepository,
        userApi,
        teamQueryService,
        teamMemberQueryService,
        pwaContactRepository,
        userAccountService,
        energyPortalServiceAccessService,
        energyPortalAccountsMessagePublishingService,
        configProperties
    ));
  }

  @BeforeAll
  static void beforeAll() {
    regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeamUser1RoleManage = new TeamRole();
    regTeamUser1RoleManage.setTeam(regTeam);
    regTeamUser1RoleManage.setWuaId(USER_1_WUA_ID);
    regTeamUser1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    orgTeam1 = new Team(UUID.randomUUID());
    orgTeam1.setTeamType(TeamType.ORGANISATION);
    orgTeam1User1RoleManage = new TeamRole();
    orgTeam1User1RoleManage.setTeam(orgTeam1);
    orgTeam1User1RoleManage.setWuaId(USER_1_WUA_ID);
    orgTeam1User1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    orgTeam2 = new Team(UUID.randomUUID());
    orgTeam2.setTeamType(TeamType.ORGANISATION);
    orgTeam2User1RoleManage = new TeamRole();
    orgTeam2User1RoleManage.setTeam(orgTeam2);
    orgTeam2User1RoleManage.setWuaId(USER_1_WUA_ID);
    orgTeam2User1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    cgTeam1 = new Team(UUID.randomUUID());
    cgTeam1.setTeamType(TeamType.CONSULTEE);
    cgTeam1User1RoleManage = new TeamRole();
    cgTeam1User1RoleManage.setTeam(cgTeam1);
    cgTeam1User1RoleManage.setWuaId(USER_1_WUA_ID);
    cgTeam1User1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    cgTeam2 = new Team(UUID.randomUUID());
    cgTeam2.setTeamType(TeamType.CONSULTEE);
    cgTeam2User1RoleManage = new TeamRole();
    cgTeam2User1RoleManage.setTeam(cgTeam2);
    cgTeam2User1RoleManage.setWuaId(USER_1_WUA_ID);
    cgTeam2User1RoleManage.setRole(Role.TEAM_ADMINISTRATOR);

    user1 = new User();
    user1.setWebUserAccountId(USER_1_WUA_ID);
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

    var newTeam = new Team(UUID.randomUUID());
    newTeam.setName("foo");
    newTeam.setTeamType(TeamType.ORGANISATION);
    newTeam.setScopeType("ORGGRP");
    newTeam.setScopeId("1");

    when(teamRepository.save(any())).thenReturn(newTeam);

    teamManagementService.createScopedTeam("foo", TeamType.ORGANISATION, scopeRef);

    verify(teamRepository).save(teamArgumentCaptor.capture());
    var teamCapture = teamArgumentCaptor.getValue();

    assertThat(teamCapture.getName()).isEqualTo("foo");
    assertThat(teamCapture.getTeamType()).isEqualTo(TeamType.ORGANISATION);
    assertThat(teamCapture.getScopeType()).isEqualTo(scopeRef.getType());
    assertThat(teamCapture.getScopeId()).isEqualTo(scopeRef.getId());

    var expectedServiceProviderTeamDto = new ServiceProviderTeamDto(
        newTeam.getId().toString(),
        newTeam.getScopeId(),
        ScopeType.ORGANISATION_GROUP,
        newTeam.getTeamType().name()
    );
    verify(energyPortalAccountsMessagePublishingService).publishTeam(expectedServiceProviderTeamDto);
  }

  @Test
  void createScopedTeam_wrongType() {
    var scopeRef = TeamScopeReference.from("1", "ORGGRP");

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.createScopedTeam("foo", TeamType.REGULATOR, scopeRef));
    verify(teamRepository, never()).save(any());
    verify(energyPortalAccountsMessagePublishingService, never()).publishTeam(any());
  }

  @Test
  void createScopedTeam_whenConsulteeAndNoAssociatedOrgGroup_thenDontPublish() {
    var scopeRef = TeamScopeReference.from("1", "CONSULTEE");


    var newTeam = new Team(UUID.randomUUID());
    newTeam.setName("foo");
    newTeam.setTeamType(TeamType.ORGANISATION);
    newTeam.setScopeType("ORGGRP");
    newTeam.setScopeId("999");

    when(teamRepository.save(any())).thenReturn(newTeam);

    teamManagementService.createScopedTeam("foo", TeamType.CONSULTEE, scopeRef);

    verify(teamRepository).save(teamArgumentCaptor.capture());
    var teamCapture = teamArgumentCaptor.getValue();

    assertThat(teamCapture.getName()).isEqualTo("foo");
    assertThat(teamCapture.getTeamType()).isEqualTo(TeamType.CONSULTEE);
    assertThat(teamCapture.getScopeType()).isEqualTo(scopeRef.getType());
    assertThat(teamCapture.getScopeId()).isEqualTo(scopeRef.getId());

    verify(energyPortalAccountsMessagePublishingService, never()).publishTeam(any());
  }

  @Test
  void createScopedTeam_whenConsulteeAndHasAssociatedOrgGroup_thenPublish() {
    var scopeRef = TeamScopeReference.from("2", "CONSULTEE");


    var newTeam = new Team(UUID.randomUUID());
    newTeam.setName("foo");
    newTeam.setTeamType(TeamType.CONSULTEE);
    newTeam.setScopeType("ORGGRP");
    newTeam.setScopeId("2");

    when(teamRepository.save(any())).thenReturn(newTeam);

    teamManagementService.createScopedTeam("foo", TeamType.CONSULTEE, scopeRef);

    verify(teamRepository).save(teamArgumentCaptor.capture());
    var teamCapture = teamArgumentCaptor.getValue();

    assertThat(teamCapture.getName()).isEqualTo("foo");
    assertThat(teamCapture.getTeamType()).isEqualTo(TeamType.CONSULTEE);
    assertThat(teamCapture.getScopeType()).isEqualTo(scopeRef.getType());
    assertThat(teamCapture.getScopeId()).isEqualTo(scopeRef.getId());

    var expectedServiceProviderTeamDto = new ServiceProviderTeamDto(
        newTeam.getId().toString(),
        configProperties.consulteeGroupIdToScopeIdAndType().get(2).scopeId().toString(),
        configProperties.consulteeGroupIdToScopeIdAndType().get(2).scopeType(),
        newTeam.getTeamType().name()
    );
    verify(energyPortalAccountsMessagePublishingService).publishTeam(expectedServiceProviderTeamDto);
  }

  @Test
  void createScopedTeam_alreadyExists() {
    var scopeRef = TeamScopeReference.from("1", "ORGGRP");

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION, "ORGGRP", "1"))
        .thenReturn(Optional.of(orgTeam1));

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.createScopedTeam("foo", TeamType.ORGANISATION, scopeRef));

    verify(teamRepository, never()).save(any());
    verify(energyPortalAccountsMessagePublishingService, never()).publishTeam(any());
  }

  @Test
  void updateTeamName() {
    var newName = "New Team Name";

    teamManagementService.updateTeamName(orgTeam1, newName);
    verify(teamRepository).save(teamArgumentCaptor.capture());

    assertThat(teamArgumentCaptor.getValue().getName()).isEqualTo(newName);
  }

  @Test
  void getTeamTypesUserIsMemberOf() {

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage, orgTeam2User1RoleManage));

    assertThat(teamManagementService.getTeamTypesUserIsMemberOf(USER_1_WUA_ID))
        .containsExactlyInAnyOrder(TeamType.REGULATOR, TeamType.ORGANISATION);
  }

  @Test
  void getStaticTeamOfTypeUserCanManage() {
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage, orgTeam2User1RoleManage));

    assertThat(teamManagementService.getStaticTeamOfTypeUserCanManage(TeamType.REGULATOR, USER_1_WUA_ID))
        .hasValue(regTeam);
  }

  @Test
  void getStaticTeamOfTypeUserCanManage_notStatic() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.getStaticTeamOfTypeUserCanManage(TeamType.ORGANISATION, USER_1_WUA_ID));
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_OrgTeam() {
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage));

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION, USER_1_WUA_ID))
        .containsExactlyInAnyOrder(orgTeam1);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_ConsulteeGroupTeam() {
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, cgTeam1User1RoleManage));

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.CONSULTEE, USER_1_WUA_ID))
        .containsExactlyInAnyOrder(cgTeam1);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_regulatorWithRoleCanManageAllOrgs() {
    // User has direct manage team role in reg team and org team 1
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage));

    // User has the special create/manage any org team priv
    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    // There are 2 org teams
    when(teamRepository.findByTeamType(TeamType.ORGANISATION))
        .thenReturn(List.of(orgTeam1, orgTeam2));

    // Verify they can manage both org team 1 and 2
    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION, USER_1_WUA_ID))
        .containsExactlyInAnyOrder(orgTeam1, orgTeam2);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_regulatorWithRoleCanManageAllConsulteeGroups() {
    // User has direct manage team role in reg team and org team 1
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(regTeamUser1RoleManage, cgTeam1User1RoleManage));

    // User has the special create/manage any org team priv
    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(true);

    // There are 2 cg teams
    when(teamRepository.findByTeamType(TeamType.CONSULTEE))
        .thenReturn(List.of(cgTeam1, cgTeam2));

    // Verify they can manage both org team 1 and 2
    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.CONSULTEE, USER_1_WUA_ID))
        .containsExactlyInAnyOrder(cgTeam1, cgTeam2);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_notScoped() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.REGULATOR, USER_1_WUA_ID));
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
  void getScopedTeam() {
    var team = TeamTestUtil.newBuilder().build();
    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION, "ORGGRP", "1"))
        .thenReturn(Optional.of(team));
    assertThat(teamManagementService.getScopedTeam(TeamType.ORGANISATION, TeamScopeReference.from("1", "ORGGRP")))
        .isEqualTo(Optional.of(team));
  }

  @Test
  void getEnergyPortalUser() {
    var expectedProjection = new UsersProjectionRoot()
        .webUserAccountId()
        .isAccountShared()
        .canLogin();

    teamManagementService.getEnergyPortalUser("foo");

    verify(userApi).searchUsersByEmail(eq("foo"), refEq(expectedProjection), any(RequestPurpose.class), any(LogCorrelationId.class));
  }

  @Test
  void setUserTeamRoles_existingUser() {
    var webUserAccount = stubWebUserAccount();
    when(userAccountService.getWebUserAccount(USER_1_WUA_ID.intValue())).thenReturn(webUserAccount);

    var expectedProjection = new UserProjectionRoot()
        .isAccountShared()
        .canLogin();
    when(userApi.findUserById(eq(1L), refEq(expectedProjection), any(RequestPurpose.class), any(LogCorrelationId.class)))
        .thenReturn(Optional.of(user1));
    when(teamRoleRepository.findAllByWuaId(1L)).thenReturn(List.of(new TeamRole()));

    long instigatingUser = 2L;
    teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam,
        List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER), instigatingUser);

    verify(teamRoleRepository).deleteByWuaIdAndTeam(USER_1_WUA_ID, regTeam);
    verify(teamRoleRepository).saveAll(teamRoleListCaptor.capture());
    verify(energyPortalServiceAccessService, never()).addUser(anyLong());
    verify(energyPortalAccountsMessagePublishingService).publishUsersRolesForTeam(
        USER_1_WUA_ID,
        regTeam.getId().toString(),
        regTeam.getTeamType().name(),
        Set.of(Role.TEAM_ADMINISTRATOR.name(), Role.ORGANISATION_MANAGER.name())
    );

    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getTeam)
        .contains(regTeam, regTeam);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getWuaId)
        .contains(USER_1_WUA_ID, USER_1_WUA_ID);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getRole)
        .contains(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER);
  }

  @NotNull
  private static WebUserAccount stubWebUserAccount() {
    var webUserAccount = new WebUserAccount();
    var person = new Person();
    webUserAccount.setPerson(person);
    return webUserAccount;
  }

  @Test
  void setUserTeamRoles_invalidRoles() {
    long instigatingUser = 2;
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, List.of(Role.APPLICATION_CREATOR),
            instigatingUser));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
    verify(energyPortalServiceAccessService, never()).addUser(anyLong());
    verify(energyPortalAccountsMessagePublishingService, never()).publishUsersRolesForTeam(
        anyLong(),
        any(),
        any(),
        any()
    );
  }

  @Test
  void setUserTeamRoles_noEpaUser() {
    when(userApi.findUserById(eq(1L), any(), any(RequestPurpose.class), any(LogCorrelationId.class)))
        .thenReturn(Optional.empty());

    long instigatingUser = 2L;
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam,
            List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER), instigatingUser));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
    verify(energyPortalServiceAccessService, never()).addUser(anyLong());
    verify(energyPortalAccountsMessagePublishingService, never()).publishUsersRolesForTeam(
        anyLong(),
        any(),
        any(),
        any()
    );
  }

  @Test
  void setUserTeamRoles_sharedAccount() {
    var epaUser = new User();
    epaUser.setIsAccountShared(true);

    when(userApi.findUserById(eq(1L), any(), any(RequestPurpose.class), any(LogCorrelationId.class)))
        .thenReturn(Optional.empty());

    long instigatingUser = 2L;
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam,
            List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER), instigatingUser));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
    verify(energyPortalServiceAccessService, never()).addUser(anyLong());
    verify(energyPortalAccountsMessagePublishingService, never()).publishUsersRolesForTeam(
        anyLong(),
        any(),
        any(),
        any()
    );
  }

  @Test
  void setUserTeamRoles_canNotLogin() {
    var epaUser = new User();
    epaUser.setCanLogin(false);

    when(userApi.findUserById(eq(1L), any(), any(RequestPurpose.class), any(LogCorrelationId.class)))
        .thenReturn(Optional.empty());

    long instigatingUser = 2L;
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam,
            List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER), instigatingUser));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
    verify(energyPortalServiceAccessService, never()).addUser(anyLong());
    verify(energyPortalAccountsMessagePublishingService, never()).publishUsersRolesForTeam(
        anyLong(),
        any(),
        any(),
        any()
    );
  }


  @Test
  void setUserTeamRoles_newUser() {
    when(userApi.findUserById(eq(USER_1_WUA_ID), any(), eq(new RequestPurpose("Validate user account")), any(LogCorrelationId.class)))
        .thenReturn(Optional.of(user1));

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of());

    when(userAccountService.getWebUserAccount(Math.toIntExact(USER_1_WUA_ID))).thenReturn(stubWebUserAccount());

    teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, List.of(Role.TEAM_ADMINISTRATOR), USER_1_WUA_ID);

    verify(energyPortalServiceAccessService).addUser(USER_1_WUA_ID);
    verify(energyPortalAccountsMessagePublishingService).publishUsersRolesForTeam(
        USER_1_WUA_ID,
        regTeam.getId().toString(),
        regTeam.getTeamType().name(),
        Set.of(Role.TEAM_ADMINISTRATOR.name())
    );
  }


  @Test
  void removeUserFromTeam_whenUserNotInOtherTeams() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    // AND they do not have another role in another team
    when(teamRoleRepository.findAllByWuaId(USER_2_WUA_ID))
        .thenReturn(List.of());

    var accessManagerRole = new TeamRole();
    accessManagerRole.setWuaId(10L);
    accessManagerRole.setRole(Role.TEAM_ADMINISTRATOR);

    // AND an access manager still exists
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(accessManagerRole));

    teamManagementService.removeUserFromTeam(USER_2_WUA_ID, regTeam);

    verify(teamRoleRepository).deleteByWuaIdAndTeam(USER_2_WUA_ID, regTeam);
    verify(energyPortalServiceAccessService).removeUser(USER_2_WUA_ID);
    verify(energyPortalAccountsMessagePublishingService).publishRemoveUserFromTeam(
        USER_2_WUA_ID,
        regTeam.getId().toString()
    );
  }

  @Test
  void removeUserFromTeam_stillInTeams() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));
    var teamRole = new TeamRole();
    when(teamRoleRepository.findAllByWuaId(USER_2_WUA_ID)).thenReturn(List.of(teamRole));

    teamManagementService.removeUserFromTeam(USER_2_WUA_ID, regTeam);
    verify(teamRoleRepository).deleteByWuaIdAndTeam(USER_2_WUA_ID, regTeam);
    verifyNoInteractions(energyPortalServiceAccessService);
    verify(energyPortalServiceAccessService, never()).removeUser(anyLong());
    verify(energyPortalAccountsMessagePublishingService).publishRemoveUserFromTeam(
        USER_2_WUA_ID,
        regTeam.getId().toString()
    );
  }

  @Test
  void removeUserFromTeam_lastTeamManager() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.removeUserFromTeam(USER_1_WUA_ID, regTeam));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(USER_1_WUA_ID, regTeam);
    verify(energyPortalServiceAccessService, never()).removeUser(anyLong());
    verify(energyPortalAccountsMessagePublishingService, never()).publishRemoveUserFromTeam(
        anyLong(),
        any()
    );
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, USER_2_WUA_ID,
        List.of(Role.ORGANISATION_MANAGER)))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate_newRolesIncludeManage() {
    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, USER_1_WUA_ID,
        List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER)))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate_noManageRoleLeft() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, USER_1_WUA_ID,
        List.of(Role.ORGANISATION_MANAGER)))
        .isFalse();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRemoval() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, USER_2_WUA_ID))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRemoval_noManageRoleLeft() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, USER_1_WUA_ID))
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

    boolean result = teamManagementService.canAddUserToTeam(USER_1_WUA_ID, staticTeam);

    assertThat(result).isTrue();
  }

  @Test
  void canAddUserToTeam_scopedTeamType_singleTeamUserNotMember_returnsTrue() {
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.CONSULTEE); // scoped type with SINGLE_TEAM restriction

    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeam.getTeamType(), USER_1_WUA_ID))
        .thenReturn(Collections.emptySet());

    boolean result = teamManagementService.canAddUserToTeam(USER_1_WUA_ID, scopedTeam);

    assertThat(result).isTrue();
  }

  @Test
  void canAddUserToTeam_scopedTeamType_singleTeamUserIsMember_returnsFalse() {
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.CONSULTEE); // scoped type with SINGLE_TEAM restriction

    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeam.getTeamType(), USER_1_WUA_ID))
        .thenReturn(Set.of(new Team()));

    boolean result = teamManagementService.canAddUserToTeam(USER_1_WUA_ID, scopedTeam);

    assertThat(result).isFalse();
  }

  @Test
  void canAddUserToTeam_scopedTeamType_multipleTeamsAlwaysTrue() {
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION); // scoped type with MULTIPLE_TEAMS restriction

    boolean result = teamManagementService.canAddUserToTeam(USER_1_WUA_ID, scopedTeam);

    assertThat(result).isTrue();
  }

  @Test
  void userCanManageAnyOrganisationTeam_whenHasRole_thenTrue() {

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    assertThat(teamManagementService.userCanManageAnyOrganisationTeam(USER_1_WUA_ID)).isTrue();
  }

  @Test
  void userCanManageAnyOrganisationTeam_whenNoRole_thenFalse() {

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.userCanManageAnyOrganisationTeam(USER_1_WUA_ID)).isFalse();
  }

  @Test
  void userCanManageAnyConsulteeGroupTeam_whenHasRole_thenTrue() {

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(true);

    assertThat(teamManagementService.userCanManageAnyConsulteeGroupTeam(USER_1_WUA_ID)).isTrue();
  }

  @Test
  void userCanManageAnyConsulteeGroupTeam_whenNoRole_thenFalse() {

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.userCanManageAnyConsulteeGroupTeam(USER_1_WUA_ID)).isFalse();
  }

  @Test
  void userCanManageSecondaryRegulatorTeam_whenHasRole_thenTrue() {
    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.SECONDARY_REGULATOR_MANAGER))
        .thenReturn(true);

    assertThat(teamManagementService.userCanManageSecondaryRegulatorTeam(USER_1_WUA_ID)).isTrue();
  }

  @Test
  void userCanManageSecondaryRegulatorTeam_whenNoRole_thenFalse() {
    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.SECONDARY_REGULATOR_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.userCanManageSecondaryRegulatorTeam(USER_1_WUA_ID)).isFalse();
  }

  @Test
  void isMemberOfTeam_whenMemberOfTeam_thenTrue() {

    when(teamRoleRepository.existsByTeamAndWuaId(regTeam, USER_1_WUA_ID))
        .thenReturn(true);

    assertThat(teamManagementService.isMemberOfTeam(regTeam, USER_1_WUA_ID)).isTrue();
  }

  @Test
  void isMemberOfTeam_whenNotMemberOfTeam_thenFalse() {

    when(teamRoleRepository.existsByTeamAndWuaId(regTeam, USER_1_WUA_ID))
        .thenReturn(false);

    assertThat(teamManagementService.isMemberOfTeam(regTeam, USER_1_WUA_ID)).isFalse();
  }

  @Test
  void canManageTeam_whenScopedTeam_andCanManageTeam_thenTrue() {

    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION);

    var teamRole = new TeamRole();
    teamRole.setTeam(scopedTeam);
    teamRole.setRole(Role.TEAM_ADMINISTRATOR);

    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(teamRole));

    assertThat(teamManagementService.canManageTeam(scopedTeam, USER_1_WUA_ID)).isTrue();
  }

  @Test
  void canManageTeam_whenScopedTeam_andCannotManageTeam_thenFalse() {

    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION);

    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of());

    assertThat(teamManagementService.canManageTeam(scopedTeam, USER_1_WUA_ID)).isFalse();
  }

  @Test
  void canManageTeam_whenOrganisationScopedTeam_andCannotManageTeam_andHasManageAnyOrganisationTeamRole_thenTrue() {

    // GIVEN a scoped organisation team
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION);

    // AND the user doesn't have the manage team permission in that team
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of());

    // WHEN the user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM role in the regulator team
    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(TeamType.ORGANISATION))
        .thenReturn(List.of(scopedTeam));

    // THEN the user can manage the team
    assertThat(teamManagementService.canManageTeam(scopedTeam, USER_1_WUA_ID)).isTrue();
  }

  @Test
  void canManageTeam_whenConsulteeGroupScopedTeam_andCannotManageTeam_andHasManageAnyConsulteeGroupTeamRole_thenTrue() {

    // GIVEN a scoped organisation team
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.CONSULTEE);

    // AND the user doesn't have the manage team permission in that team
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of());

    // WHEN the user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM role in the regulator team
    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(TeamType.CONSULTEE))
        .thenReturn(List.of(scopedTeam));

    // THEN the user can manage the team
    assertThat(teamManagementService.canManageTeam(scopedTeam, USER_1_WUA_ID)).isTrue();
  }

  @Test
  void canManageTeam_whenStaticTeam_andCanManageTeam_thenTrue() {

    var staticTeam = new Team((UUID.randomUUID()));
    staticTeam.setTeamType(TeamType.REGULATOR);

    var teamRole = new TeamRole();
    teamRole.setTeam(staticTeam);
    teamRole.setRole(Role.TEAM_ADMINISTRATOR);

    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of(teamRole));

    assertThat(teamManagementService.canManageTeam(staticTeam, USER_1_WUA_ID)).isTrue();
  }

  @Test
  void canManageTeam_whenStaticTeam_andCannotManageTeam_thenFalse() {

    var staticTeam = new Team((UUID.randomUUID()));
    staticTeam.setTeamType(TeamType.REGULATOR);

    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.TEAM_ADMINISTRATOR))
        .thenReturn(List.of());

    assertThat(teamManagementService.canManageTeam(staticTeam, USER_1_WUA_ID)).isFalse();
  }

  @Test
  void getStaticTeamOfTypeUserCanView_whenTeamTypeIsScoped_thenException() {
    var scopedTeamType  = TeamType.ORGANISATION;

    assertThatThrownBy(() -> teamManagementService.getStaticTeamOfTypeUserCanView(scopedTeamType, USER_1_WUA_ID))
        .isInstanceOf(TeamManagementException.class)
        .hasMessage("TeamType %s is scoped, expected static".formatted(scopedTeamType));
  }

  @Test
  void getStaticTeamOfTypeUserCanView_whenInSecondaryRegulatorTeamAndRegulatorTeam() {
    var scopedTeamType  = TeamType.SECONDARY_REGULATOR;

    var secondaryRegulatorTeam =  new Team((UUID.randomUUID()));
    secondaryRegulatorTeam.setTeamType(TeamType.SECONDARY_REGULATOR);

    var team1Role = new TeamRole();
    team1Role.setTeam(secondaryRegulatorTeam);
    team1Role.setRole(Role.CONSENT_VIEWER);

    when(teamRepository.findByTeamType(TeamType.SECONDARY_REGULATOR)).thenReturn(List.of(secondaryRegulatorTeam));

    var regulatorTeam =  new Team((UUID.randomUUID()));
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var team2Role1 = new TeamRole();
    team2Role1.setTeam(regulatorTeam);
    team2Role1.setRole(Role.SECONDARY_REGULATOR_MANAGER);

    var team2Role2 =  new TeamRole();
    team2Role2.setTeam(regulatorTeam);
    team2Role2.setRole(Role.TEAM_ADMINISTRATOR);

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.SECONDARY_REGULATOR_MANAGER)).thenReturn(true);

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(team1Role, team2Role1, team2Role2));

    assertThat(teamManagementService.getStaticTeamOfTypeUserCanView(scopedTeamType, USER_1_WUA_ID))
        .contains(secondaryRegulatorTeam);
  }

  @Test
  void getStaticTeamOfTypeUserCanView_whenInSecondaryRegulatorTeamOnly() {
    var scopedTeamType  = TeamType.SECONDARY_REGULATOR;

    var secondaryRegulatorTeam =  new Team((UUID.randomUUID()));
    secondaryRegulatorTeam.setTeamType(TeamType.SECONDARY_REGULATOR);

    var team1Role = new TeamRole();
    team1Role.setTeam(secondaryRegulatorTeam);
    team1Role.setRole(Role.CONSENT_VIEWER);

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.SECONDARY_REGULATOR_MANAGER))
        .thenReturn(false);

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(team1Role));

    assertThat(teamManagementService.getStaticTeamOfTypeUserCanView(scopedTeamType, USER_1_WUA_ID))
        .contains(secondaryRegulatorTeam);
  }

  @Test
  void getStaticTeamOfTypeUserCanView_whenInRegulatorTeamOnly() {
    var scopedTeamType  = TeamType.SECONDARY_REGULATOR;

    var secondaryRegulatorTeam =  new Team((UUID.randomUUID()));
    secondaryRegulatorTeam.setTeamType(TeamType.SECONDARY_REGULATOR);

    when(teamRepository.findByTeamType(TeamType.SECONDARY_REGULATOR)).thenReturn(List.of(secondaryRegulatorTeam));

    var regulatorTeam =  new Team((UUID.randomUUID()));
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var teamRole1 = new TeamRole();
    teamRole1.setTeam(regulatorTeam);
    teamRole1.setRole(Role.SECONDARY_REGULATOR_MANAGER);

    var teamRole2 =  new TeamRole();
    teamRole2.setTeam(regulatorTeam);
    teamRole2.setRole(Role.TEAM_ADMINISTRATOR);

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.SECONDARY_REGULATOR_MANAGER))
        .thenReturn(true);

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(teamRole1, teamRole2));

    assertThat(teamManagementService.getStaticTeamOfTypeUserCanView(scopedTeamType, USER_1_WUA_ID))
        .contains(secondaryRegulatorTeam);
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenScopedTeamType_thenException() {

    var scopedTeamType = TeamType.ORGANISATION;

    assertThatThrownBy(() -> teamManagementService.getStaticTeamOfTypeUserIsMemberOf(scopedTeamType, USER_1_WUA_ID))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenNotMemberOfTeamOfType_thenEmptyOptional() {

    var staticTeamType = TeamType.REGULATOR;

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of());

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, USER_1_WUA_ID);

    assertThat(resultingTeam).isEmpty();
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenMemberOfTeamOfType_thenTeamReturned() {

    var staticTeamType = TeamType.REGULATOR;

    var expectedTeam = new Team(UUID.randomUUID());
    expectedTeam.setTeamType(staticTeamType);

    var teamRole = new TeamRole();
    teamRole.setTeam(expectedTeam);

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(teamRole));

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, USER_1_WUA_ID);

    assertThat(resultingTeam).contains(expectedTeam);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenStaticTeamType_thenException() {

    var staticTeamType = TeamType.REGULATOR;

    assertThatThrownBy(() -> teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(staticTeamType, USER_1_WUA_ID))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserNotMemberOfAnyTeamOfType_thenEmptySetReturned() {

    var scopedTeamType = TeamType.ORGANISATION;

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of());

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, USER_1_WUA_ID);

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

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(firstRoleForSecondTeam, firstRoleForFirstTeam, secondRoleForFirstTeam));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, USER_1_WUA_ID);

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

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(roleForTeamUserIsMemberOf));

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(scopedTeamType))
        .thenReturn(List.of(teamUserIsNotMemberOf, teamUserIsMemberOf));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, USER_1_WUA_ID);

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

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(roleForTeamUserIsMemberOf));

    when(teamQueryService.userHasStaticRole(USER_1_WUA_ID, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(scopedTeamType))
        .thenReturn(List.of(teamUserIsNotMemberOf, teamUserIsMemberOf));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, USER_1_WUA_ID);

    assertThat(resultingScopedTeams)
        .containsExactlyInAnyOrder(teamUserIsNotMemberOf, teamUserIsMemberOf);
  }

  @Test
  void getEnergyPortalUser_whenMultipleUsersFound_thenThrow() {
    var emailAddress = "test@test.com";

    when(userApi.searchUsersByEmail(eq(emailAddress), any(), any(RequestPurpose.class), any(LogCorrelationId.class)))
        .thenReturn(List.of(new User(), new User()));

    assertThatThrownBy(() -> teamManagementService.getEnergyPortalUser(emailAddress))
        .isInstanceOf(TeamManagementException.class)
        .hasMessage("More than one UK Energy Portal user exists with the email address %s".formatted(emailAddress));
  }
}