package uk.co.ogauthority.pwa.features.reassignment;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class CaseReassignmentOfficerValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return CaseReassignmentOfficerForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "assignedCaseOfficerPersonId",
        FieldValidationErrorCodes.REQUIRED.errorCode("assignedCaseOfficerPersonId"),
        "Select the case officer to reassign cases to");
  }
}
