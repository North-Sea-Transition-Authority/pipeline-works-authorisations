package uk.co.ogauthority.pwa.util.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

public class PipelineValidationUtils {

  private PipelineValidationUtils() {
    throw new AssertionError();
  }

  public static void validateFromToLocation(String value,
                                            Errors errors,
                                            String fieldName,
                                            String fieldLabel) {

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        fieldName,
        fieldName + ".required",
        String.format("Enter the %s", fieldLabel.toLowerCase()));

    if (value != null) {

      if (value.length() > 200) {
        errors.rejectValue(
            fieldName,
            FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode(fieldName),
            String.format("%s must be 200 characters or fewer", fieldLabel));
      }

      if (value.contains("##")) {
        errors.rejectValue(
            fieldName,
            FieldValidationErrorCodes.INVALID.errorCode(fieldName),
            String.format("%s cannot contain '##'", fieldLabel));
      }

    }

  }
}
