package uk.co.ogauthority.pwa.validators.consultations;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
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
    var consultationRequestService = (ConsultationRequestService) validationHints[0];
    var consulteeGroupTeamService = (ConsulteeGroupTeamService) validationHints[1];

    if (form.getConsulteeGroupSelection().isEmpty() && form.getOtherGroupSelected() == null) {
      errors.rejectValue("consulteeGroupSelection", "consulteeGroupSelection" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select at least one consultee group");

    } else {
      for (var selectedGroupId: form.getConsulteeGroupSelection().keySet()) {
        var consulteeGroupDetail = consulteeGroupTeamService.getConsulteeGroupDetailById(Integer.parseInt(selectedGroupId));
        if (consultationRequestService.isConsultationRequestOpen(consulteeGroupDetail)) {
          errors.rejectValue("consulteeGroupSelection", "consulteeGroupSelection" + FieldValidationErrorCodes.INVALID.getCode(),
              "A consultation for " + consulteeGroupDetail.getName() +
                  " is already open, select a group that doesn't already have an open request");
        } else {
          form.getConsulteeGroupSelection().put(selectedGroupId, "true");
        }
      }

      if (BooleanUtils.isTrue(form.getOtherGroupSelected())) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherGroupLogin",
            "otherGroupLogin" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "You must enter a valid email address or login ID");
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
