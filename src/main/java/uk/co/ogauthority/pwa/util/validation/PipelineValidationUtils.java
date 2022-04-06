package uk.co.ogauthority.pwa.util.validation;

import java.math.BigDecimal;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.PwaNumberUtils;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

public class PipelineValidationUtils {

  private PipelineValidationUtils() {
    throw new AssertionError();
  }

  private static final int MAX_IDENT_LENGTH_DP = 2;

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


  public static int getMaxIdentLengthDp() {
    return MAX_IDENT_LENGTH_DP;
  }

  public static void validateLength(BigDecimal length,
                                    Errors errors,
                                    String fieldName,
                                    String fieldLabel) {

    if (PwaNumberUtils.getNumberOfDp(length) > 2) {
      errors.rejectValue(fieldName, FieldValidationErrorCodes.MAX_DP_EXCEEDED.errorCode(fieldName),
          String.format("%s cannot have more than 2dp", fieldLabel));
    }

    if (length.compareTo(BigDecimal.ZERO) <= 0) {
      errors.rejectValue(fieldName, FieldValidationErrorCodes.INVALID.errorCode(fieldName),
          String.format("%s must be 1 or more", fieldLabel));
    }

  }

  public static PipelineIdentDataForm createEmptyPipelineIdentDataForm() {

    var dataForm = new PipelineIdentDataForm();
    dataForm.setExternalDiameter(new DecimalInput());
    dataForm.setInternalDiameter(new DecimalInput());
    dataForm.setWallThickness(new DecimalInput());
    dataForm.setMaop(new DecimalInput());
    return dataForm;
  }

}
