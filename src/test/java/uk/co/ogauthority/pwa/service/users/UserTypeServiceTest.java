package uk.co.ogauthority.pwa.service.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class UserTypeServiceTest {

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private UserTypeService userTypeService;

  private final WebUserAccount webUserAccount = new WebUserAccount(1);
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(webUserAccount, Set.of());

  @Test
  void getPriorityUserTypeOrThrow_whenNoTeam() {
    assertThrows(IllegalStateException.class, () ->
        userTypeService.getPriorityUserTypeOrThrow(user));
  }

  @Test
  void getPriorityUserTypeOrThrow_whenIndustryTeamOnly() {
    Team team = new Team();
    team.setTeamType(TeamType.ORGANISATION);
    when(teamQueryService.getTeamsUserIsMemberOf(user.getWuaId())).thenReturn(Collections.singletonList(team));

    user = new AuthenticatedUserAccount(webUserAccount, Set.of());

    assertThat(userTypeService.getPriorityUserTypeOrThrow(user)).isEqualTo(UserType.INDUSTRY);
  }

  @Test
  void getPriorityUserTypeOrThrow_whenRegulatorTeamOnly() {
    Team team = new Team();
    team.setTeamType(TeamType.REGULATOR);
    when(teamQueryService.getTeamsUserIsMemberOf(user.getWuaId())).thenReturn(Collections.singletonList(team));

    assertThat(userTypeService.getPriorityUserTypeOrThrow(user)).isEqualTo(UserType.OGA);
  }

  @Test
  void getPriorityUserTypeOrThrow_whenConsulteeTeamOnly() {
    Team team = new Team();
    team.setTeamType(TeamType.CONSULTEE);
    when(teamQueryService.getTeamsUserIsMemberOf(user.getWuaId())).thenReturn(Collections.singletonList(team));

    assertThat(userTypeService.getPriorityUserTypeOrThrow(user)).isEqualTo(UserType.CONSULTEE);
  }

  @Test
  void getUserTypes_whenAllTeams() {
    List<Team> teams = Arrays.stream(TeamType.values())
        .map(teamType -> {
          Team team = new Team();
          team.setTeamType(teamType);
          return team;
        })
        .toList();
    when(teamQueryService.getTeamsUserIsMemberOf(user.getWuaId())).thenReturn(teams);

    assertThat(userTypeService.getUserTypes(user)).containsExactlyInAnyOrder(UserType.values());
  }

  @Test
  void getUserTypes_whenNoTeams() {
    when(teamQueryService.getTeamsUserIsMemberOf(user.getWuaId())).thenReturn(Collections.emptyList());
    assertThat(userTypeService.getUserTypes(user)).isEmpty();
  }
}
