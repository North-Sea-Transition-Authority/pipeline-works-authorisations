package uk.co.ogauthority.pwa.validators.pwaapplications.shared.submission;

import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.submission.ApplicationUpdateResponseForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ApplicationUpdateResponseFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ApplicationUpdateResponseForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (ApplicationUpdateResponseForm) target;
    ValidationUtils.rejectIfEmpty(
        errors,
        "madeOnlyRequestedChanges",
        REQUIRED.errorCode("madeOnlyRequestedChanges"),
        "Select the option which describes your update"
    );

    if (BooleanUtils.isFalse(form.getMadeOnlyRequestedChanges())) {
      ValidationUtils.rejectIfEmpty(
          errors,
          "otherChangesDescription",
          REQUIRED.errorCode("otherChangesDescription"),
          "Enter a description of the changes"
      );

      ValidatorUtils.validateDefaultStringLength(
          errors, "otherChangesDescription", form::getOtherChangesDescription, "Description of changes");

    }

  }
}
