package uk.co.ogauthority.pwa.validators.consultations;


import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class AssignResponderValidator implements SmartValidator {



  @Override
  public boolean supports(Class<?> clazz) {
    return AssignResponderForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (AssignResponderForm) target;
    AssignResponderValidationHints assignResponderValidationHints = (AssignResponderValidationHints) validationHints[0];
    var assignResponderService = assignResponderValidationHints.getAssignResponderService();
    var consultationRequest = assignResponderValidationHints.getConsultationRequest();

    ValidationUtils.rejectIfEmpty(errors,"responderPersonId", FieldValidationErrorCodes.REQUIRED.errorCode("responderPersonId"),
        "You must select a responder for the consultation");

    if (form.getResponderPersonId() != null)  {
      List<Person> responders = assignResponderService.getAllRespondersForRequest(consultationRequest);
      var matchedResponders = responders.stream()
          .filter(person -> person.getId().asInt() == form.getResponderPersonId())
          .collect(Collectors.toList());
      if (matchedResponders.isEmpty()) {
        errors.rejectValue("responderPersonId", "responderPersonId" + FieldValidationErrorCodes.INVALID.getCode(),
            "You must select a valid responder for the consultation");
      }
    }




  }




  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }
}
