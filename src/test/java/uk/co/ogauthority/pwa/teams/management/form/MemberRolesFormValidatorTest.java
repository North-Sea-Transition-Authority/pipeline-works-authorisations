package uk.co.ogauthority.pwa.teams.management.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@ExtendWith(MockitoExtension.class)
class MemberRolesFormValidatorTest {

  @Mock
  private TeamManagementService teamManagementService;

  @InjectMocks
  private MemberRolesFormValidator memberRolesFormValidator;

  private MemberRolesForm form;
  private BeanPropertyBindingResult errors;
  private Team team;

  @BeforeEach
  void setUp() {
    form = new MemberRolesForm();
    errors = new BeanPropertyBindingResult(form, "form");
    team = new Team(UUID.randomUUID());
  }

  @Test
  void isValid() {
    form.setRoles(List.of("TEAM_ADMINISTRATOR"));

    when(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(team, 1L, List.of(Role.TEAM_ADMINISTRATOR)))
        .thenReturn(true);

    assertThat(memberRolesFormValidator.isValid(form,1L, team, errors)).isTrue();
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void isValid_noRoles() {
    form.setRoles(null);

    assertThat(memberRolesFormValidator.isValid(form,1L, team, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_noTeamManagerLeft() {
    form.setRoles(List.of("TEAM_ADMINISTRATOR"));

    when(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(team, 1L, List.of(Role.TEAM_ADMINISTRATOR)))
        .thenReturn(false);

    assertThat(memberRolesFormValidator.isValid(form,1L, team, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

}
