package uk.co.ogauthority.pwa.validators.consultations;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class ConsultationRequestValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return ConsultationRequestForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (ConsultationRequestForm) target;
    var consultationRequestValidationHints = (ConsultationRequestValidationHints) validationHints[0];
    var consulteeGroupDetailService = consultationRequestValidationHints.getConsulteeGroupDetailService();
    var consultationRequestService = consultationRequestValidationHints.getConsultationRequestService();
    var pwaApplication = consultationRequestValidationHints.getPwaApplication();


    if (form.getConsulteeGroupSelection().isEmpty()) {
      //using first index of consulteeGroupSelection in order for the fds banner error message to jump to the id of the first checkbox
      errors.rejectValue("consulteeGroupSelection[1]", "consulteeGroupSelection" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select at least one consultee group");

    } else {
      for (var selectedGroupId: form.getConsulteeGroupSelection().keySet()) {
        var consulteeGroupDetail = consulteeGroupDetailService.getConsulteeGroupDetailById(Integer.parseInt(selectedGroupId));
        if (consultationRequestService.isConsultationRequestOpen(consulteeGroupDetail.getConsulteeGroup(), pwaApplication)) {
          errors.rejectValue("consulteeGroupSelection", "consulteeGroupSelection" + FieldValidationErrorCodes.INVALID.getCode(),
              "A consultation for " + consulteeGroupDetail.getName() +
                  " is already open, select a group that doesn't already have an open request");
        }
      }
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "daysToRespond", "daysToRespond" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must enter the number of calendar days");
    if (form.getDaysToRespond() != null && form.getDaysToRespond() < 1) {
      errors.rejectValue("daysToRespond", "daysToRespond" + FieldValidationErrorCodes.INVALID.getCode(),
          "You must enter a valid amount of calendar days");
    }




  }




  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }
}
