package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.AddConsulteeGroupTeamMemberForm;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;

@Service
public class AddConsulteeGroupTeamMemberFormValidator implements SmartValidator {

  private final TeamManagementService teamManagementService;
  private final ConsulteeGroupTeamService groupTeamService;

  @Autowired
  public AddConsulteeGroupTeamMemberFormValidator(TeamManagementService teamManagementService,
                                                  ConsulteeGroupTeamService groupTeamService) {
    this.teamManagementService = teamManagementService;
    this.groupTeamService = groupTeamService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(AddConsulteeGroupTeamMemberForm.class);
  }

  @Override
  @Deprecated
  public void validate(Object target, Errors errors) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (AddConsulteeGroupTeamMemberForm) target;
    var consulteeGroupDetail = (ConsulteeGroupDetail) validationHints[0];

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userIdentifier", "userIdentifier.required",
        "Enter an email address or login ID");

    if (StringUtils.isNotEmpty(form.getUserIdentifier())) {

      Optional<Person> person = teamManagementService.getPersonByEmailAddressOrLoginId(form.getUserIdentifier());

      if (person.isEmpty()) {
        errors.rejectValue("userIdentifier", "userIdentifier.userNotFound", "User not found");
      } else {

        // check if the person is already a member of the team
        boolean alreadyMember = groupTeamService.getTeamMembersForGroup(consulteeGroupDetail.getConsulteeGroup()).stream()
            .anyMatch(teamMember -> Objects.equals(teamMember.getPerson(), person.get()));

        if (alreadyMember) {
          errors.rejectValue("userIdentifier", "userIdentifier.userAlreadyExists", "This person is already a member of this team");
        }

      }
    }

  }

}
