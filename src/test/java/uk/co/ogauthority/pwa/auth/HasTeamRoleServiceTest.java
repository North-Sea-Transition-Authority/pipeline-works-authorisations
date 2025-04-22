package uk.co.ogauthority.pwa.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
public class HasTeamRoleServiceTest {

  private final AuthenticatedUserAccount USER = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private HasTeamRoleService underTest;

  @Test
  void canAccessTeamManagement_IsMemberOfAnyTeam_ReturnsTrue() {
    when(teamQueryService.userIsMemberOfAnyTeam(USER.getWuaId())).thenReturn(true);

    boolean result = underTest.userIsMemberOfAnyTeam(USER);

    assertThat(result).isTrue();
  }

  @Test
  void canAccessTeamManagement_IsNotMemberOfAnyTeam_ReturnsFalse() {
    when(teamQueryService.userIsMemberOfAnyTeam(USER.getWuaId())).thenReturn(false);

    boolean result = underTest.userIsMemberOfAnyTeam(USER);

    assertThat(result).isFalse();
  }

  @Test
  void userHasAnyRoleInTeamTypes_HasRoleInAllTeamTypes_ReturnsTrue() {
    lenient().when(teamQueryService.userHasAtLeastOneRole(eq((long) USER.getWuaId()), eq(TeamType.REGULATOR), anySet()))
        .thenReturn(true);
    lenient().when(teamQueryService.userHasAtLeastOneRole(eq((long) USER.getWuaId()), eq(TeamType.ORGANISATION), anySet()))
        .thenReturn(true);
    lenient().when(teamQueryService.userHasAtLeastOneRole(eq((long) USER.getWuaId()), eq(TeamType.CONSULTEE), anySet()))
        .thenReturn(true);

    boolean result = underTest.userHasAnyRoleInTeamTypes(USER, RoleGroup.APPLICATION_SEARCH.getRolesByTeamType());

    assertThat(result).isTrue();
  }

  @Test
  void userHasAnyRoleInTeamTypes_HasRoleInAnyTeamType_ReturnsTrue() {
    when(teamQueryService.userHasAtLeastOneRole(eq((long) USER.getWuaId()), eq(TeamType.REGULATOR), anySet()))
        .thenReturn(true);
    lenient().when(teamQueryService.userHasAtLeastOneRole(eq((long) USER.getWuaId()), eq(TeamType.ORGANISATION), anySet()))
        .thenReturn(false);
    lenient().when(teamQueryService.userHasAtLeastOneRole(eq((long) USER.getWuaId()), eq(TeamType.CONSULTEE), anySet()))
        .thenReturn(false);

    boolean result = underTest.userHasAnyRoleInTeamTypes(USER, RoleGroup.APPLICATION_SEARCH.getRolesByTeamType());

    assertThat(result).isTrue();
  }

  @Test
  void userHasAnyRoleInTeamTypes_HasRoleInNoneOfTeamTypes_ReturnsFalse() {
    when(teamQueryService.userHasAtLeastOneRole(eq((long) USER.getWuaId()), eq(TeamType.REGULATOR), anySet()))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneRole(eq((long) USER.getWuaId()), eq(TeamType.ORGANISATION), anySet()))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneRole(eq((long) USER.getWuaId()), eq(TeamType.CONSULTEE), anySet()))
        .thenReturn(false);

    boolean result = underTest.userHasAnyRoleInTeamTypes(USER, RoleGroup.APPLICATION_SEARCH.getRolesByTeamType());

    assertThat(result).isFalse();
  }

}