package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.form.masterpwas.contacts.AddPwaContactForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;

@Service
public class AddPwaContactFormValidator implements Validator {

  private static final String USER_ID_FORM_FIELD = "userIdentifier";

  private final TeamManagementService teamManagementService;
  private final PwaContactService pwaContactService;
  private final PwaApplicationService pwaApplicationService;

  @Autowired
  public AddPwaContactFormValidator(TeamManagementService teamManagementService,
                                    PwaContactService pwaContactService,
                                    PwaApplicationService pwaApplicationService) {
    this.teamManagementService = teamManagementService;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationService = pwaApplicationService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return AddPwaContactForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    var form = (AddPwaContactForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        USER_ID_FORM_FIELD,
        FieldValidationErrorCodes.REQUIRED.errorCode(USER_ID_FORM_FIELD),
        "Enter an email address or login ID");

    if (StringUtils.isNotEmpty(form.getUserIdentifier())) {

      Optional<Person> person = teamManagementService.getPersonByEmailAddressOrLoginId(form.getUserIdentifier());

      if (person.isPresent()) {
        // check if the person is already a contact on the PWA
        var application = pwaApplicationService.getApplicationFromId(form.getPwaApplicationId());
        Person personToBeAdded = person.get();
        if (pwaContactService.personIsContactOnApplication(application, personToBeAdded)) {
          errors.rejectValue(
              USER_ID_FORM_FIELD,
              USER_ID_FORM_FIELD + ".userAlreadyExists",
              "This person is already a contact for this application");
        }
      } else {
        errors.rejectValue(
            USER_ID_FORM_FIELD,
            USER_ID_FORM_FIELD + ".userNotFound",
            "User not found");
      }
    }

  }

}
