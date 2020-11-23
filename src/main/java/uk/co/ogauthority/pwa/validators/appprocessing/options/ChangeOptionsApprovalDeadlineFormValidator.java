package uk.co.ogauthority.pwa.validators.appprocessing.options;


import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.appprocessing.options.ChangeOptionsApprovalDeadlineForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ChangeOptionsApprovalDeadlineFormValidator implements Validator {

  private static final String NOTE_ATTR = "note";

  @Override
  public boolean supports(Class<?> clazz) {
    return ChangeOptionsApprovalDeadlineForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (ChangeOptionsApprovalDeadlineForm) target;

    ValidatorUtils.validateDateIsPresentOrFuture(
        "deadlineDate",
        "deadline",
        form.getDeadlineDateDay(),
        form.getDeadlineDateMonth(),
        form.getDeadlineDateYear(),
        errors
    );

    ValidatorUtils.validateDefaultStringLength(errors, NOTE_ATTR, () -> form.getNote(), "Change reason");
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, NOTE_ATTR, REQUIRED.errorCode(NOTE_ATTR),"Enter reason for changing deadline");

  }
}
