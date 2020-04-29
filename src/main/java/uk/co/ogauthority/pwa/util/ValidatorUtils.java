package uk.co.ogauthority.pwa.util;

import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

public class ValidatorUtils {

  public ValidatorUtils() {
    throw new AssertionError();
  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   * @param fieldPrefix The prefix of the form date fields. EG: proposedStartDay has a prefix of proposedStart.
   * @param displayPrefix The grouped name in the error message. EG: "proposed start".
   * @param day Form field day
   * @param month Form field month
   * @param year Form field year
   * @param errors Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDate(String fieldPrefix,
                                     String displayPrefix,
                                     @Nullable Integer day,
                                     @Nullable Integer month,
                                     @Nullable Integer year,
                                     Errors errors) {
    var dayValid = Range.between(1, 31).contains(day);
    var monthValid = Range.between(1, 12).contains(month);
    var yearValid = year != null && year >= 0;
    if (dayValid && monthValid && yearValid) {
      try {
        LocalDate.of(year, month, day);
      } catch (DateTimeException e) {
        errors.rejectValue(fieldPrefix + "Day", String.format("%sDay%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()),
            String.format("Enter a valid %s day", displayPrefix));
        errors.rejectValue(fieldPrefix + "Month", String.format("%sMonth%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()), "");
        errors.rejectValue(fieldPrefix + "Year", String.format("%sYear%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()), "");
        return false;
      }
      return true;
    } else {
      errors.rejectValue(fieldPrefix + "Day", String.format("%sDay%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()),
          String.format("Enter a valid %s date", displayPrefix));
      errors.rejectValue(fieldPrefix + "Month", String.format("%sMonth%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()), "");
      errors.rejectValue(fieldPrefix + "Year", String.format("%sYear%s", fieldPrefix, FieldValidationErrorCodes.INVALID.getCode()), "");
      return false;
    }
  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   * Ensures that the date is valid, and the date is either the current day, or is in the future.
   * @param fieldPrefix The prefix of the form date fields. EG: proposedStartDay has a prefix of proposedStart.
   * @param displayPrefix The grouped name in the error message. EG: "proposed start".
   * @param day Form field day
   * @param month Form field month
   * @param year Form field year
   * @param errors Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDateIsPresentOrFuture(String fieldPrefix,
                                                      String displayPrefix,
                                                      Integer day,
                                                      Integer month,
                                                      Integer year,
                                                      Errors errors) {
    if (validateDate(fieldPrefix, displayPrefix, day, month, year, errors)) {
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
    }
    return false;
  }

  public static boolean validateDateIsPastOrPresent(String fieldPrefix,
                                                      String displayPrefix,
                                                      Integer day,
                                                      Integer month,
                                                      Integer year,
                                                      Errors errors) {
    if (validateDate(fieldPrefix, displayPrefix, day, month, year, errors)) {
      var date = LocalDate.of(year, month, day);
      if (date.isAfter(LocalDate.now())) {
        errors.rejectValue(fieldPrefix + "Day", String.format("%sDay%s", fieldPrefix, FieldValidationErrorCodes.AFTER_TODAY.getCode()),
            String.format("%s must not be in the future", StringUtils.capitalize(displayPrefix)));
        errors.rejectValue(fieldPrefix + "Month",
            String.format("%sMonth%s", fieldPrefix, FieldValidationErrorCodes.AFTER_TODAY.getCode()), "");
        errors.rejectValue(fieldPrefix + "Year",
            String.format("%sYear%s", fieldPrefix, FieldValidationErrorCodes.AFTER_TODAY.getCode()), "");
        return false;
      }
      return true;
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

  /**
   * Ensures that a Boolean object is true.
   * @param errors Errors object
   * @param field The field name matching the form's field name.
   * @param errorMessage The message to display if invalid.
   */
  public static void validateBooleanTrue(Errors errors, Boolean bool, String field, String errorMessage) {
    if (!BooleanUtils.toBooleanDefaultIfNull(bool, false)) {
      errors.rejectValue(field, field + ".required", errorMessage);
    }
  }

  public static void validateLatitude(Errors errors,
                                      Pair<String, Integer> degrees,
                                      Pair<String, Integer> minutes,
                                      Pair<String, BigDecimal> seconds) {

    if (degrees.getValue() == null || minutes.getValue() == null || seconds.getValue() == null) {
      errors.rejectValue(degrees.getKey(), degrees.getKey() + REQUIRED.getCode(),
          "Enter the latitude details");
      errors.rejectValue(minutes.getKey(), minutes.getKey() + REQUIRED.getCode(), "");
      errors.rejectValue(seconds.getKey(), seconds.getKey() + REQUIRED.getCode(), "");
      return;
    }

    var messagePrefix = "Latitude";

    if (!Range.between(45, 64).contains(degrees.getValue())) {
      errors.rejectValue(degrees.getKey(), degrees.getKey() + INVALID.getCode(),
          String.format("%s degrees should be between 45 and 64", messagePrefix));
    }

    validateMinutes(errors, minutes, messagePrefix);
    validateSeconds(errors, seconds, messagePrefix);

  }

  private static void validateMinutes(Errors errors, Pair<String, Integer> minutes, String messagePrefix) {

    if (!Range.between(0, 59).contains(minutes.getValue())) {
      errors.rejectValue(minutes.getKey(), minutes.getKey() + INVALID.getCode(),
          String.format("%s minutes should be between 0 and 59", messagePrefix));
    }

  }

  private static void validateSeconds(Errors errors, Pair<String, BigDecimal> seconds, String messagePrefix) {

    if (! ((seconds.getValue().compareTo(BigDecimal.ZERO) >= 0) && (seconds.getValue().compareTo(BigDecimal.valueOf(60)) < 0))) {
      errors.rejectValue(seconds.getKey(), seconds.getKey() + INVALID.getCode(),
          "%s seconds should be between 0 and 59.99");
    }

    if (seconds.getValue().remainder(BigDecimal.ONE).precision() > 2) {
      errors.rejectValue(seconds.getKey(), seconds.getKey() + INVALID.getCode(),
          String.format("%s seconds should not have more than 2dp", messagePrefix));
    }

  }

  public static void validateLongitude(Errors errors,
                                       Pair<String, Integer> degrees,
                                       Pair<String, Integer> minutes,
                                       Pair<String, BigDecimal> seconds,
                                       Pair<String, LongitudeDirection> direction) {

    if (degrees.getValue() == null || minutes.getValue() == null || seconds.getValue() == null || direction.getValue() == null) {
      errors.rejectValue(degrees.getKey(), degrees.getKey() + REQUIRED.getCode(),
          "Enter the longitude details");
      errors.rejectValue(minutes.getKey(), minutes.getKey() + REQUIRED.getCode(), "");
      errors.rejectValue(seconds.getKey(), seconds.getKey() + REQUIRED.getCode(), "");
      errors.rejectValue(direction.getKey(), direction.getKey() + REQUIRED.getCode(), "");
      return;
    }

    var messagePrefix = "Longitude";

    if (!Range.between(0, 30).contains(degrees.getValue())) {
      errors.rejectValue(degrees.getKey(), degrees.getKey() + INVALID.getCode(),
          String.format("%s degrees should be between 0 and 30", messagePrefix));
    }

    validateMinutes(errors, minutes, messagePrefix);
    validateSeconds(errors, seconds, messagePrefix);

  }
}
