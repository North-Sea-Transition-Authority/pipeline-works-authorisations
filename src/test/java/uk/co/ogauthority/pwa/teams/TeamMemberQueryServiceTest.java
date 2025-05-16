package uk.co.ogauthority.pwa.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.UsersProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

@ExtendWith(MockitoExtension.class)
class TeamMemberQueryServiceTest {
  @Mock
  private TeamRoleRepository teamRoleRepository;

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private UserApi userApi;

  @Spy
  @InjectMocks
  private TeamMemberQueryService teamMemberQueryService;

  private static Team regTeam;

  private static TeamRole regTeamUser1RoleManage;
  private static TeamRole regTeamUser1RoleOrgAdmin;
  private static TeamRole regTeamUser2RoleOrgAdmin;

  private static final Long user1WuaId = 1L;
  private static User user1;
  private static final Long user2WuaId = 2L;
  private static User user2;

  @BeforeEach
  void setUp() {
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

    var teamMemberView = teamMemberQueryService.getTeamMemberView(regTeam, user1WuaId);

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

    var teamMemberViews = teamMemberQueryService.getTeamMemberViewsForTeam(regTeam);

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
  void getTeamMemberViewsByTeamRoles() {
    var teamA = new Team(UUID.randomUUID());
    teamA.setTeamType(TeamType.ORGANISATION);

    var teamB = new Team(UUID.randomUUID());
    teamB.setTeamType(TeamType.ORGANISATION);

    var teamC = new Team(UUID.randomUUID());
    teamC.setTeamType(TeamType.REGULATOR);

    var teamRole1 = new TeamRole(UUID.randomUUID());
    teamRole1.setWuaId(1L);
    teamRole1.setTeam(teamA);
    teamRole1.setRole(Role.TEAM_ADMINISTRATOR);

    var teamRole2 = new TeamRole(UUID.randomUUID());
    teamRole2.setWuaId(1L);
    teamRole2.setTeam(teamC);
    teamRole2.setRole(Role.PWA_MANAGER);

    var teamRole3 = new TeamRole(UUID.randomUUID());
    teamRole3.setWuaId(2L);
    teamRole3.setTeam(teamB);
    teamRole3.setRole(Role.TEAM_ADMINISTRATOR);

    var teamRole4 = new TeamRole(UUID.randomUUID());
    teamRole4.setWuaId(2L);
    teamRole4.setTeam(teamC);
    teamRole4.setRole(Role.TEAM_ADMINISTRATOR);

    var teamRole5 = new TeamRole(UUID.randomUUID());
    teamRole5.setWuaId(2L);
    teamRole5.setTeam(teamC);
    teamRole5.setRole(Role.ORGANISATION_MANAGER);

    var expectedProjection = new UsersProjectionRoot()
        .webUserAccountId()
        .title()
        .forename()
        .surname()
        .primaryEmailAddress()
        .telephoneNumber();

    var user1 = new User();
    user1.setWebUserAccountId(1);
    user1.setTitle("Ms");
    user1.setForename("Test");
    user1.setSurname("User");
    user1.setPrimaryEmailAddress("test@example.com");
    user1.setTelephoneNumber("0123456789");

    var user2 = new User();
    user2.setWebUserAccountId(2);
    user2.setTitle("Mr");
    user2.setForename("Example");
    user2.setSurname("User");
    user2.setPrimaryEmailAddress("example@example.com");
    user2.setTelephoneNumber("9876543210");

    when(userApi.searchUsersByIds(eq(List.of(1, 2)), refEq(expectedProjection), any(RequestPurpose.class)))
        .thenReturn(List.of(user1, user2));

    var result = teamMemberQueryService.getTeamMemberViewsByTeamRoles(List.of(teamRole1, teamRole2, teamRole3, teamRole4, teamRole5));

    assertThat(result)
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
        .containsExactlyInAnyOrder(
            tuple(
                Long.valueOf(user1.getWebUserAccountId()),
                user1.getTitle(),
                user1.getForename(),
                user1.getSurname(),
                user1.getPrimaryEmailAddress(),
                user1.getTelephoneNumber(),
                teamA.getId(),
                List.of(Role.TEAM_ADMINISTRATOR)
            ),
            tuple(
                Long.valueOf(user1.getWebUserAccountId()),
                user1.getTitle(),
                user1.getForename(),
                user1.getSurname(),
                user1.getPrimaryEmailAddress(),
                user1.getTelephoneNumber(),
                teamC.getId(),
                List.of(Role.PWA_MANAGER)
            ),
            tuple(
                Long.valueOf(user2.getWebUserAccountId()),
                user2.getTitle(),
                user2.getForename(),
                user2.getSurname(),
                user2.getPrimaryEmailAddress(),
                user2.getTelephoneNumber(),
                teamB.getId(),
                List.of(Role.TEAM_ADMINISTRATOR)
            ),
            tuple(
                Long.valueOf(user2.getWebUserAccountId()),
                user2.getTitle(),
                user2.getForename(),
                user2.getSurname(),
                user2.getPrimaryEmailAddress(),
                user2.getTelephoneNumber(),
                teamC.getId(),
                List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER)
            )
        );
  }

  @Test
  void getUserTeamRolesViewsFrom() {
    var teamA = new Team(UUID.randomUUID());
    teamA.setScopeId("scopeA");
    teamA.setTeamType(TeamType.ORGANISATION);

    var teamB = new Team(UUID.randomUUID());
    teamB.setScopeId("scopeB");
    teamB.setTeamType(TeamType.ORGANISATION);

    var teamC = new Team(UUID.randomUUID());
    teamC.setScopeId("scopeC");
    teamC.setTeamType(TeamType.REGULATOR);

    var teamRole1 = new TeamRole(UUID.randomUUID());
    teamRole1.setWuaId(1L);
    teamRole1.setTeam(teamA);
    teamRole1.setRole(Role.TEAM_ADMINISTRATOR);

    var teamRole2 = new TeamRole(UUID.randomUUID());
    teamRole2.setWuaId(1L);
    teamRole2.setTeam(teamC);
    teamRole2.setRole(Role.PWA_MANAGER);

    var teamRole3 = new TeamRole(UUID.randomUUID());
    teamRole3.setWuaId(2L);
    teamRole3.setTeam(teamB);
    teamRole3.setRole(Role.TEAM_ADMINISTRATOR);

    var teamRole4 = new TeamRole(UUID.randomUUID());
    teamRole4.setWuaId(2L);
    teamRole4.setTeam(teamC);
    teamRole4.setRole(Role.TEAM_ADMINISTRATOR);

    var teamRole5 = new TeamRole(UUID.randomUUID());
    teamRole5.setWuaId(2L);
    teamRole5.setTeam(teamC);
    teamRole5.setRole(Role.ORGANISATION_MANAGER);

    var result = teamMemberQueryService.getUserTeamRolesViewsFrom(List.of(teamRole1, teamRole2, teamRole3, teamRole4, teamRole5));

    assertThat(result)
        .extracting(
            UserTeamRolesView::wuaId,
            UserTeamRolesView::teamId,
            UserTeamRolesView::teamScopeId,
            UserTeamRolesView::roles
        )
        .containsExactlyInAnyOrder(
            tuple(
                1L,
                teamA.getId(),
                "scopeA",
                List.of(Role.TEAM_ADMINISTRATOR)
            ),
            tuple(
                1L,
                teamC.getId(),
                "scopeC",
                List.of(Role.PWA_MANAGER)
            ),
            tuple(
                2L,
                teamB.getId(),
                "scopeB",
                List.of(Role.TEAM_ADMINISTRATOR)
            ),
            tuple(
                2L,
                teamC.getId(),
                "scopeC",
                List.of(Role.TEAM_ADMINISTRATOR, Role.ORGANISATION_MANAGER)
            )
        );
  }


  @Test
  void getTeamMemberViewsByTeamAndRole() {
    var role = regTeamUser1RoleManage.getRole();
    var teamMember = TeamMemberView.fromEpaUser(user1, regTeam.getId(), List.of(role));
    var teamRoles = List.of(regTeamUser1RoleManage);

    when(teamRoleRepository.findByTeamAndRole(regTeam, role)).thenReturn(teamRoles);
    doReturn(List.of(teamMember)).when(teamMemberQueryService).getTeamMemberViewsByTeamRoles(teamRoles);

    var teamMemberViews = teamMemberQueryService.getTeamMemberViewsByTeamAndRole(regTeam, role);

    assertThat(teamMemberViews)
        .isNotNull()
        .hasSize(1);
    verify(teamRoleRepository).findByTeamAndRole(regTeam, role);
  }

  @Test
  void getTeamMemberViewsByTeamAndRole_NoRoles() {
    var team = mock(Team.class);
    var role = Role.TEAM_ADMINISTRATOR;

    when(teamRoleRepository.findByTeamAndRole(team, role)).thenReturn(Collections.emptyList());

    var teamMemberViews = teamMemberQueryService.getTeamMemberViewsByTeamAndRole(team, role);

    assertThat(teamMemberViews)
        .isNotNull()
        .isEmpty();
    verify(teamRoleRepository).findByTeamAndRole(team, role);
  }

  @Test
  void getTeamMemberViewsByScopedTeam() {
    var teamType = TeamType.ORGANISATION;
    var scopeId = "1";
    Team team = mock(Team.class);
    var teamMember = TeamMemberView.fromEpaUser(user1, regTeam.getId(), List.of(regTeamUser1RoleManage.getRole()));

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, teamType.getScopeType(), scopeId))
        .thenReturn(Optional.of(team));
    when(teamMemberQueryService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMember));

    var teamMemberViews = teamMemberQueryService.getTeamMemberViewsByScopedTeam(teamType, TeamScopeReference.from(scopeId, teamType));

    assertThat(teamMemberViews)
        .containsExactly(teamMember);
    verify(teamRepository).findByTeamTypeAndScopeTypeAndScopeId(teamType, teamType.getScopeType(), scopeId);
  }

  @Test
  void getTeamMemberViewsByScopedTeam_NoTeams() {
    var teamType = TeamType.ORGANISATION;
    var scopeId = "1";

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, teamType.getScopeType(), scopeId))
        .thenReturn(Optional.empty());

    var teamMemberViews = teamMemberQueryService.getTeamMemberViewsByScopedTeam(teamType, TeamScopeReference.from(scopeId, teamType));

    assertThat(teamMemberViews).isEmpty();
    verify(teamRepository).findByTeamTypeAndScopeTypeAndScopeId(teamType, teamType.getScopeType(), scopeId);
  }
}