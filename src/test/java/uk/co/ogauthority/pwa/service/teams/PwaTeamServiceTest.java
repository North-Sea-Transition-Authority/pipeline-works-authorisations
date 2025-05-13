package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.UserTeamRolesView;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

@ExtendWith(MockitoExtension.class)
class PwaTeamServiceTest {

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private UserAccountService userAccountService;

  @InjectMocks
  private PwaTeamService pwaTeamService;

  private final Role ROLE = Role.TEAM_ADMINISTRATOR;
  private ConsulteeGroup consulteeGroup;

  @BeforeEach
  void setUp() {
    consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(123);
  }

  @Test
  void getMembersWithRegulatorRole_returnsTeamMembers() {
    List<TeamMemberView> expectedMembers = List.of(
        new TeamMemberView(1L, "Mr", "John", "Doe", "john.doe@example.com", "1234567890", null, List.of(ROLE))
    );

    when(teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, ROLE))
        .thenReturn(expectedMembers);

    List<TeamMemberView> actualMembers = pwaTeamService.getMembersWithRegulatorRole(ROLE);

    assertThat(actualMembers).isEqualTo(expectedMembers);
  }

  @Test
  void getPeopleWithRegulatorRole_returnsPersons() {
    Set<UserTeamRolesView> userTeamRolesViews = Set.of(
        new UserTeamRolesView(1L, null, null, List.of(ROLE))
    );
    Set<Integer> wuaIdSet = Set.of(1);

    Set<Person> expectedPersons = Set.of(new Person(1, "John", "Doe", "john.doe@example.com", "1234567890"));

    when(teamQueryService.getUsersOfStaticTeamWithRole(TeamType.REGULATOR, ROLE))
        .thenReturn(List.copyOf(userTeamRolesViews));
    when(userAccountService.getPersonsByWuaIdSet(wuaIdSet))
        .thenReturn(expectedPersons);

    Set<Person> actualPersons = pwaTeamService.getPeopleWithRegulatorRole(ROLE);

    assertThat(actualPersons).isEqualTo(expectedPersons);
  }

  @Test
  void getTeamMembersWithRegulatorRole_returnsTeamMembers() {
    List<TeamMemberView> expectedMembers = List.of(
        new TeamMemberView(2L, "Ms", "Jane", "Smith", "jane.smith@example.com", "0987654321", null, List.of(ROLE))
    );

    when(teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, ROLE))
        .thenReturn(expectedMembers);

    List<TeamMemberView> actualMembers = pwaTeamService.getTeamMembersWithRegulatorRole(ROLE);

    assertThat(actualMembers).isEqualTo(expectedMembers);
  }

  @Test
  void getPeopleByConsulteeGroupAndRoleIn_returnsPersons() {
    Set<Role> roles = Set.of(Role.RECIPIENT, Role.RESPONDER);
    var teamType = TeamType.CONSULTEE;

    List<UserTeamRolesView> userTeamRolesViews = List.of(
        new UserTeamRolesView(3L, null, null, List.of(Role.RECIPIENT)),
        new UserTeamRolesView(4L, null, null, List.of(Role.RESPONDER))
    );

    Set<Integer> wuaIdSet = Set.of(3, 4);

    Set<Person> expectedPersons = Set.of(
        new Person(3, "Alice", "Brown", "alice.brown@example.com", "1112223333"),
        new Person(4, "Bob", "White", "bob.white@example.com", "4445556666")
    );

    when(teamQueryService.getUsersOfScopedTeam(eq(teamType), any(TeamScopeReference.class)))
        .thenReturn(userTeamRolesViews);
    when(userAccountService.getPersonsByWuaIdSet(wuaIdSet))
        .thenReturn(expectedPersons);

    Set<Person> actualPersons = pwaTeamService.getPeopleByConsulteeGroupAndRoleIn(consulteeGroup, roles);

    assertThat(actualPersons).isEqualTo(expectedPersons);
  }
}