package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.AddConsulteeGroupTeamMemberForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
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
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("not implemented");
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

        // check that person isn't in any other groups or already part of this group
        groupTeamService.getTeamMemberByPerson(person.get())
            .ifPresent(groupTeamMember -> {

              if (Objects.equals(groupTeamMember.getConsulteeGroup(), consulteeGroupDetail.getConsulteeGroup())) {

                errors.rejectValue("userIdentifier", "userIdentifier.userAlreadyExists", "This person is already a member of this team");

              } else {

                errors.rejectValue("userIdentifier",
                    FieldValidationErrorCodes.INVALID.errorCode("userIdentifier"), "This person cannot be added to this team");

              }

            });

      }
    }

  }

}
