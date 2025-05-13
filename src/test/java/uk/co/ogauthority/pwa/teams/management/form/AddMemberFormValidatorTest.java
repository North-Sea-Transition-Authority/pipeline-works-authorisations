package uk.co.ogauthority.pwa.teams.management.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@ExtendWith(MockitoExtension.class)
class AddMemberFormValidatorTest {

  @Mock
  private TeamManagementService teamManagementService;

  @InjectMocks
  private AddMemberFormValidator addMemberFormValidator;

  private AddMemberForm form;
  private User user;
  private BeanPropertyBindingResult errors;
  private UUID teamId;
  private Team team;

  @BeforeEach
  void setUp() {
    form = new AddMemberForm();

    user = new User();
    user.setWebUserAccountId(1);

    teamId = UUID.randomUUID();
    team = new Team(teamId);
    team.setTeamType(TeamType.REGULATOR);

    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void isValid() {
    setupValidForm();
    setupValidUser();

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));
    when(teamManagementService.getTeam(teamId)).thenReturn(Optional.of(team));

    assertThat(addMemberFormValidator.isValid(form, teamId, errors)).isTrue();
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void isValid_noUsername() {
    var errors = new BeanPropertyBindingResult(form, "form");
    form.setUsername(null);
    assertThat(addMemberFormValidator.isValid(form, teamId, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_noEpaUser() {
    setupValidForm();

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of());

    assertThat(addMemberFormValidator.isValid(form, teamId, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_tooManyEpaUsers() {
    setupValidForm();

    var user1 = new User();
    user1.setIsAccountShared(false);
    user1.setCanLogin(true);

    var user2 = new User();
    user2.setIsAccountShared(false);
    user2.setCanLogin(true);

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user1, user2));
    when(teamManagementService.getTeam(teamId)).thenReturn(Optional.of(team));

    assertThat(addMemberFormValidator.isValid(form, teamId, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_sharedAccount() {
    setupValidForm();
    user.setIsAccountShared(true);
    user.setCanLogin(true);

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));
    when(teamManagementService.getTeam(teamId)).thenReturn(Optional.of(team));

    assertThat(addMemberFormValidator.isValid(form, teamId, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_canNotLogin() {
    setupValidForm();
    user.setIsAccountShared(false);
    user.setCanLogin(false);

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));
    when(teamManagementService.getTeam(teamId)).thenReturn(Optional.of(team));

    assertThat(addMemberFormValidator.isValid(form, teamId, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_throwsException_whenTeamNotFound() {
    setupValidForm();
    setupValidUser();

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));
    when(teamManagementService.getTeam(teamId)).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalStateException.class, () -> addMemberFormValidator.isValid(form, teamId, errors));

    assertThat(exception.getMessage()).contains(teamId.toString());
  }

  @Test
  void isValid_rejectsAlreadyInScopedTeam() {
    setupValidForm();
    setupValidUser();

    team.setTeamType(TeamType.CONSULTEE);

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));
    when(teamManagementService.getTeam(teamId)).thenReturn(Optional.of(team));
    when(teamManagementService.canAddUserToTeam(user.getWebUserAccountId(), team)).thenReturn(false);

    var result = addMemberFormValidator.isValid(form, teamId, errors);

    assertThat(result).isFalse();
    assertThat(errors.getFieldError("username").getCode()).isEqualTo("username.alreadyInTeamType");
  }

  @Test
  void isValid_allowsWhenTeamNotScoped() {
    setupValidForm();
    setupValidUser();

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));
    when(teamManagementService.getTeam(teamId)).thenReturn(Optional.of(team));

    var valid = addMemberFormValidator.isValid(form, teamId, errors);

    assertThat(valid).isTrue();
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void isValid_allowsWhenScopedTeamAndUserCanBeAdded() {
    setupValidForm();
    setupValidUser();

    team.setTeamType(TeamType.CONSULTEE);

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));
    when(teamManagementService.getTeam(teamId)).thenReturn(Optional.of(team));
    when(teamManagementService.canAddUserToTeam(user.getWebUserAccountId(), team)).thenReturn(true);

    var valid = addMemberFormValidator.isValid(form, teamId, errors);

    assertThat(valid).isTrue();
    assertThat(errors.hasErrors()).isFalse();
  }

  private void setupValidForm() {
    form.setUsername("foo");
  }

  private void setupValidUser() {
    user.setIsAccountShared(false);
    user.setCanLogin(true);
  }
}
