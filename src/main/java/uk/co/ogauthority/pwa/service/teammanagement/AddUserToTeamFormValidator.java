package uk.co.ogauthority.pwa.service.teammanagement;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.form.teammanagement.AddUserToTeamForm;
import uk.co.ogauthority.pwa.model.teams.PwaTeam;

@Service
public class AddUserToTeamFormValidator implements Validator {

  private final OldTeamManagementService teamManagementService;

  @Autowired
  public AddUserToTeamFormValidator(OldTeamManagementService teamManagementService) {
    this.teamManagementService = teamManagementService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return AddUserToTeamForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    AddUserToTeamForm form = (AddUserToTeamForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userIdentifier", "userIdentifier.required",
        "Enter an email address or login ID");

    if (StringUtils.isNotEmpty(form.getUserIdentifier())) {

      Optional<Person> person = teamManagementService.getPersonByEmailAddressOrLoginId(form.getUserIdentifier());

      if (person.isEmpty()) {
        errors.rejectValue("userIdentifier", "userIdentifier.userNotFound", "User not found");
      } else {
        // check if the person is already member of the team
        PwaTeam team = teamManagementService.getTeamOrError(form.getResId());
        Person teamUser = person.get();
        if (teamManagementService.isPersonMemberOfTeam(teamUser, team)) {
          errors.rejectValue("userIdentifier", "userIdentifier.userAlreadyExists", "This person is already a member of this team");
        }
      }
    }
  }
}