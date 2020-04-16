package uk.co.ogauthority.pwa.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.function.Supplier;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

public class ValidatorUtils {

  public ValidatorUtils() {
    throw new AssertionError();
  }

  public static boolean validateDate(String fieldPrefix,
                                     String displayPrefix,
                                     Integer day,
                                     Integer month,
                                     Integer year,
                                     Errors errors) {
    var dayValid = Range.between(1, 31).contains(day);
    var monthValid = Range.between(1, 12).contains(month);
    var yearValid = year != null && year >= 0;
    if (dayValid && monthValid && yearValid) {
      return true;
    } else {
      errors.rejectValue(fieldPrefix + "Day", String.format("%sDay%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()),
          String.format("Enter a valid %s date", displayPrefix));
      errors.rejectValue(fieldPrefix + "Month", String.format("%sMonth%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()), "");
      errors.rejectValue(fieldPrefix + "Year", String.format("%sYear%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()), "");
      return false;
    }
  }

  public static boolean validateDateIsPresentOrFuture(String fieldPrefix,
                                                      String displayPrefix,
                                                      Integer day,
                                                      Integer month,
                                                      Integer year,
                                                      Errors errors) {
    if (validateDate(fieldPrefix, displayPrefix, day, month, year, errors)) {
      try {
        var date = LocalDate.of(year, month, day);
        if (date.isBefore(LocalDate.now())) {
          errors.rejectValue(fieldPrefix + "Day", String.format("%sDay%s", fieldPrefix, FieldValidationErrorCodes.BEFORE_TODAY.getCode()),
              String.format("%s must not be in the past", StringUtils.capitalize(displayPrefix)));
          errors.rejectValue(fieldPrefix + "Month",
              String.format("%sMonth%s", fieldPrefix, FieldValidationErrorCodes.BEFORE_TODAY.getCode()), "");
          errors.rejectValue(fieldPrefix + "Year",
              String.format("%sYear%s", fieldPrefix, FieldValidationErrorCodes.BEFORE_TODAY.getCode()), "");
          return false;
        }
        return true;
      } catch (DateTimeException dte) {
        errors.rejectValue(fieldPrefix + "Day", String.format("%sDay%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()),
            String.format("Enter a valid %s day", displayPrefix));
        errors.rejectValue(fieldPrefix + "Month", String.format("%sMonth%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()), "");
        errors.rejectValue(fieldPrefix + "Year", String.format("%sYear%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()), "");
        return false;
      }
    }
    return false;
  }


  public static void validateDefaultStringLength(Errors errors,
                                                 String field,
                                                 Supplier<String> stringSupplier,
                                                 String messagePrefix) {

    validateMaxLength(errors, field, stringSupplier, messagePrefix + " must be 4000 characters or fewer", 4000);

  }

  private static void validateMaxLength(Errors errors,
                                        String fieldName,
                                        Supplier<String> stringSupplier,
                                        String message,
                                        int maxLength) {
    var testString = stringSupplier.get();
    if (testString != null && testString.length() > maxLength) {
      errors.rejectValue(fieldName, fieldName + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode(), message);
    }
  }

  public static void validateEmailIfPresent(Errors errors, String field, Supplier<String> email, String messagePrefix) {
    var emailAddress = email.get();
    if (emailAddress != null && !EmailValidator.getInstance().isValid(emailAddress)) {
      errors.rejectValue(field, field + FieldValidationErrorCodes.INVALID.getCode(), messagePrefix + " must be a valid email");
    }

  }

}
