package uk.co.ogauthority.pwa.service.teammanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.form.teammanagement.AddUserToTeamForm;
import uk.co.ogauthority.pwa.model.teams.PwaTeam;
import uk.co.ogauthority.pwa.util.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class AddUserToTeamFormValidatorTest {

  @Mock
  private TeamManagementService teamManagementService;

  private AddUserToTeamFormValidator addUserToTeamFormValidator;
  private AddUserToTeamForm addUserToTeamForm;
  private Person foundPerson;
  private PwaTeam team;

  @Before
  public void setup() {
    addUserToTeamFormValidator = new AddUserToTeamFormValidator(teamManagementService);
    addUserToTeamForm = new AddUserToTeamForm();
    team = TeamTestingUtils.getRegulatorTeam();
    foundPerson = new Person(1, "Found", "Person", "found@person.com");
  }

  @Test
  public void validate_userIdentifierHasError_whenEmpty() {
    addUserToTeamForm.setUserIdentifier("");
    var errors = new BeanPropertyBindingResult(addUserToTeamForm, "form");
    ValidationUtils.invokeValidator(addUserToTeamFormValidator, addUserToTeamForm, errors);

    assertThat(errors.hasFieldErrors("userIdentifier")).isTrue();
    verifyNoInteractions(teamManagementService);
  }

  @Test
  public void validate_userIdentifierHasError_whenNull() {
    addUserToTeamForm.setUserIdentifier(null);
    var errors = new BeanPropertyBindingResult(addUserToTeamForm, "form");
    ValidationUtils.invokeValidator(addUserToTeamFormValidator, addUserToTeamForm, errors);

    assertThat(errors.hasFieldErrors("userIdentifier")).isTrue();
    verifyNoInteractions(teamManagementService);
  }

  @Test
  public void validate_userIdentifierHasError_whenPersonNotFound() {
    var identifier = "personWhoDoesNotExist";
    addUserToTeamForm.setUserIdentifier(identifier);
    var errors = new BeanPropertyBindingResult(addUserToTeamForm, "form");
    ValidationUtils.invokeValidator(addUserToTeamFormValidator, addUserToTeamForm, errors);

    assertThat(errors.hasFieldErrors("userIdentifier")).isTrue();
    verify(teamManagementService, times(1)).getPersonByEmailAddressOrLoginId(identifier);
  }

  @Test
  public void validate_userIdentifierHasError_whenPersonFound_andAlreadyMemberOfTeam() {
    when(teamManagementService.getPersonByEmailAddressOrLoginId(foundPerson.getEmailAddress()))
        .thenReturn(Optional.of(foundPerson));
    when(teamManagementService.getTeamOrError(team.getId())).thenReturn(team);
    when(teamManagementService.isPersonMemberOfTeam(foundPerson, team)).thenReturn(true);

    addUserToTeamForm.setUserIdentifier(foundPerson.getEmailAddress());
    addUserToTeamForm.setResId(team.getId());

    Errors errors = new BeanPropertyBindingResult(addUserToTeamForm, "form");
    ValidationUtils.invokeValidator(addUserToTeamFormValidator, addUserToTeamForm, errors);
    assertThat(errors.hasFieldErrors("userIdentifier")).isTrue();
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void validate_errorWhenTeamNotfound() {
    when(teamManagementService.getPersonByEmailAddressOrLoginId(foundPerson.getEmailAddress()))
        .thenReturn(Optional.of(foundPerson));
    when(teamManagementService.getTeamOrError(999)).thenThrow(new PwaEntityNotFoundException(""));
    addUserToTeamForm.setUserIdentifier(foundPerson.getEmailAddress());
    addUserToTeamForm.setResId(999);

    Errors errors = new BeanPropertyBindingResult(addUserToTeamForm, "form");
    ValidationUtils.invokeValidator(addUserToTeamFormValidator, addUserToTeamForm, errors);
  }

  @Test
  public void validate_noErrors_whenPersonFound_andTeamKnown_andNotAlreadyMemberOfTeam() {
    when(teamManagementService.getPersonByEmailAddressOrLoginId(foundPerson.getEmailAddress()))
        .thenReturn(Optional.of(foundPerson));
    when(teamManagementService.getTeamOrError(team.getId())).thenReturn(team);
    when(teamManagementService.isPersonMemberOfTeam(foundPerson, team)).thenReturn(false);
    addUserToTeamForm.setUserIdentifier(foundPerson.getEmailAddress());
    addUserToTeamForm.setResId(team.getId());

    Errors errors = new BeanPropertyBindingResult(addUserToTeamForm, "form");
    ValidationUtils.invokeValidator(addUserToTeamFormValidator, addUserToTeamForm, errors);
    assertThat(errors.hasErrors()).isFalse();
  }

}
