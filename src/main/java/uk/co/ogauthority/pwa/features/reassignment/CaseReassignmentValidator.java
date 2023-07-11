package uk.co.ogauthority.pwa.features.reassignment;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class CaseReassignmentValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return CaseReassignmentSelectorForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "selectedApplicationIds",
        FieldValidationErrorCodes.REQUIRED.errorCode("selectedApplicationIds"),
        "Return to the previous page to add PWA's to reassign.");

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "assignedCaseOfficerPersonId",
        FieldValidationErrorCodes.REQUIRED.errorCode("assignedCaseOfficerPersonId"),
        "Select the Case Officer to reassign cases to.");
  }
}
