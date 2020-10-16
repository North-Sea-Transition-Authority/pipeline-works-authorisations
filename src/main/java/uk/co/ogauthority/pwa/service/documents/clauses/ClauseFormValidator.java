package uk.co.ogauthority.pwa.service.documents.clauses;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class ClauseFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ClauseForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils.rejectIfEmpty(errors, "name", FieldValidationErrorCodes.REQUIRED.errorCode("name"), "Enter a clause name");

    ValidationUtils.rejectIfEmpty(errors, "text", FieldValidationErrorCodes.REQUIRED.errorCode("text"), "Enter some clause text");

  }
}
