package uk.co.ogauthority.pwa.validators.appprocessing.appupdaterequest;


import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.appprocessing.applicationupdate.ApplicationUpdateRequestForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ApplicationUpdateRequestValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ApplicationUpdateRequestForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    var form = (ApplicationUpdateRequestForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestReason", REQUIRED.errorCode("requestReason"),
        "A reason for the update request must be provided");
    ValidatorUtils.validateDefaultStringLength(errors, "requestReason", form::getRequestReason, "The reason for update request");
    ValidatorUtils.validateDatePickerDateIsPresentOrFuture("deadlineTimestampStr", "Deadline date", form.getDeadlineTimestampStr(), errors);
  }

}
