package uk.co.ogauthority.pwa.util;

import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.function.Supplier;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinateUtils;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

public class ValidatorUtils {

  public static final String DATE_REQUIRED_ERROR_FORMAT = "Enter a %s date";
  public static final String MONTH_YEAR_INVALID_ERROR_FORMAT = "Enter a valid %s month (1-12) and year (4 digits)";
  private static final String DATE_INVALID_ERROR_FORMAT = "Enter a valid %s date using the format day " +
      "(1-31), month (1-12), year (4 digits)";

  public static final int MAX_DEFAULT_STRING_LENGTH = 3900;
  public static final String MAX_DEFAULT_STRING_LENGTH_MESSAGE = " must be " + MAX_DEFAULT_STRING_LENGTH + " characters or fewer";

  private ValidatorUtils() {
    throw new AssertionError();
  }

  /**
   * invoke validator on a nested object while safely pushing and popping the nested object path.
   */
  public static void invokeNestedValidator(Errors errors,
                                           Validator validator,
                                           String targetPath,
                                           Object targetObject,
                                           Object... validationHints) {
    try {
      errors.pushNestedPath(targetPath);
      ValidationUtils.invokeValidator(validator, targetObject, errors, validationHints);
    } finally {
      errors.popNestedPath();
    }
  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   *
   * @param fieldPrefix   The prefix of the form date fields. EG: proposedStartDay has a prefix of proposedStart.
   * @param displayPrefix The grouped name in the error message. EG: "proposed start".
   * @param day           Form field day
   * @param month         Form field month
   * @param year          Form field year
   * @param errors        Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDate(String fieldPrefix,
                                     String displayPrefix,
                                     @Nullable Integer day,
                                     @Nullable Integer month,
                                     @Nullable Integer year,
                                     Errors errors) {
    var dayValid = Range.between(1, 31).contains(day);
    var monthValid = isMonthValid(month);
    var yearValid = isYearValid(year);
    var allNull = !ObjectUtils.anyNotNull(day, month, year);

    // when a date has all null inputs, have a single error for the whole date
    if (allNull) {
      errors.rejectValue(fieldPrefix + "Day", REQUIRED.errorCode(fieldPrefix + "Day"),
          String.format(DATE_REQUIRED_ERROR_FORMAT, displayPrefix));
      errors.rejectValue(fieldPrefix + "Month", FieldValidationErrorCodes.REQUIRED.errorCode(fieldPrefix + "Month"), "");
      errors.rejectValue(fieldPrefix + "Year", FieldValidationErrorCodes.REQUIRED.errorCode(fieldPrefix + "Year"), "");
      return false;
    }

    var dateValid = false;
    // when date as a whole not valid, mark all fields as invalid
    if (dayValid && monthValid && yearValid) {
      try {
        LocalDate.of(year, month, day);
        dateValid = true;
      } catch (DateTimeException e) {
        // do nothing, dateValid already false
      }
    }

    // when date as a whole not valid, mark all fields as invalid
    if (!dateValid) {
      errors.rejectValue(fieldPrefix + "Day", FieldValidationErrorCodes.INVALID.errorCode(fieldPrefix + "Day"),
          String.format(DATE_INVALID_ERROR_FORMAT, displayPrefix));
      errors.rejectValue(fieldPrefix + "Month", FieldValidationErrorCodes.INVALID.errorCode(fieldPrefix + "Month"), "");
      errors.rejectValue(fieldPrefix + "Year", FieldValidationErrorCodes.INVALID.errorCode(fieldPrefix + "Year"), "");
      return false;
    }

    return dateValid;

  }

  private static boolean isMonthValid(Integer month) {
    return Range.between(1, 12).contains(month);
  }

  public static boolean isYearValid(Integer year) {
    return year != null && year >= 1000 && year <= 4000;
  }

  private static boolean isDayValid(Integer day, Integer month, Integer year) {
    if (ObjectUtils.allNotNull(day, month, year)) {
      YearMonth yearMonth = YearMonth.of(year, month);
      return day <= yearMonth.lengthOfMonth();
    }
    return false;
  }

  public static void validateDateWhenPresent(String fieldPrefix,
                                             String displayPrefix,
                                             @Nullable Integer day,
                                             @Nullable Integer month,
                                             @Nullable Integer year,
                                              Errors errors) {

    if (ObjectUtils.anyNotNull(day, month, year)
        && (!isYearValid(year) || !isMonthValid(month) || !isDayValid(day, month, year))) {

      errors.rejectValue(fieldPrefix + "Day", FieldValidationErrorCodes.INVALID.errorCode(fieldPrefix + "Day"),
          String.format(DATE_INVALID_ERROR_FORMAT, displayPrefix));
      errors.rejectValue(fieldPrefix + "Month", FieldValidationErrorCodes.INVALID.errorCode(fieldPrefix + "Month"), "");
      errors.rejectValue(fieldPrefix + "Year", FieldValidationErrorCodes.INVALID.errorCode(fieldPrefix + "Year"), "");
    }
  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   * Ensures that the date is valid, and the date is either the current day, or is in the future.
   *
   * @param fieldPrefix   The prefix of the form date fields. EG: proposedStartDay has a prefix of proposedStart.
   * @param displayPrefix The grouped name in the error message. EG: "proposed start".
   * @param day           Form field day
   * @param month         Form field month
   * @param year          Form field year
   * @param errors        Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDateIsPresentOrFuture(String fieldPrefix,
                                                      String displayPrefix,
                                                      Integer day,
                                                      Integer month,
                                                      Integer year,
                                                      Errors errors) {
    return validateDateIsOnOrAfterComparisonDate(
        fieldPrefix,
        displayPrefix,
        day,
        month,
        year,
        LocalDate.now(),
        "in the past",
        errors
    );
  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   * Ensures that the date is valid and parseable.
   *
   * @param fieldName     The name of the field on the form for the error to bind to.
   * @param displayPrefix The grouped name in the error message. EG: "proposed start".
   * @param dateStr       The String date to parsed, must be in the format (dd/mm/yyyy) to be successfully parsed
   * @param errors        Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDatePickerDateExistsAndIsValid(String fieldName,
                                                                String displayPrefix,
                                                                String dateStr,
                                                                Errors errors) {
    displayPrefix = displayPrefix.toLowerCase();
    try {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, REQUIRED.errorCode(fieldName), "Enter " + displayPrefix);

      if (dateStr != null) {
        DateUtils.datePickerStringToDate(dateStr);
        return true;
      }
      return false;

    } catch (DateTimeParseException e) {
      errors.rejectValue(fieldName, FieldValidationErrorCodes.INVALID.errorCode(fieldName),
          StringUtils.capitalize(displayPrefix) + " must be a valid date in the format dd/mm/yyyy");
      return false;
    }

  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   * Ensures that the date is valid, and the date is either the current day, or is in the past.
   *
   * @param fieldName     The name of the field on the form for the error to bind to.
   * @param displayPrefix The grouped name in the error message. EG: "proposed start".
   * @param dateStr       The String date to parsed, must be in the format (dd/mm/yyyy) to be successfully parsed
   * @param errors        Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDatePickerDateIsPastOrPresent(String fieldName,
                                                                String displayPrefix,
                                                                String dateStr,
                                                                Errors errors) {
    displayPrefix = displayPrefix.toLowerCase();
    try {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, REQUIRED.errorCode(fieldName), "Enter " + displayPrefix);

      if (dateStr != null) {
        var date = DateUtils.datePickerStringToDate(dateStr);
        if (date.isAfter(LocalDate.now())) {
          errors.rejectValue(fieldName, FieldValidationErrorCodes.AFTER_TODAY.errorCode(fieldName),
              StringUtils.capitalize(displayPrefix) + " must be on or before today");
          return false;
        }
        return true;
      }

      return false;

    } catch (DateTimeParseException e) {
      errors.rejectValue(fieldName, FieldValidationErrorCodes.INVALID.errorCode(fieldName),
          StringUtils.capitalize(displayPrefix) + " must be a valid date in the format dd/mm/yyyy");
      return false;
    }

  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   * Ensures that the date is valid, and the date is either the current day, or is in the future.
   *
   * @param fieldName     The name of the field on the form for the error to bind to.
   * @param displayPrefix The grouped name in the error message. EG: "proposed start".
   * @param dateStr       The String date to parsed, must be in the format (dd/mm/yyyy) to be successfully parsed
   * @param errors        Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDatePickerDateIsPresentOrFuture(String fieldName,
                                                                String displayPrefix,
                                                                String dateStr,
                                                                Errors errors) {
    displayPrefix = displayPrefix.toLowerCase();
    try {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, REQUIRED.errorCode(fieldName), "Enter a " + displayPrefix);

      if (dateStr != null) {
        var date = DateUtils.datePickerStringToDate(dateStr);
        if (date.isBefore(LocalDate.now())) {
          errors.rejectValue(fieldName, FieldValidationErrorCodes.BEFORE_TODAY.errorCode(fieldName),
              StringUtils.capitalize(displayPrefix) + " must be on or after today");
          return false;
        }
        return true;
      }

      return false;

    } catch (DateTimeParseException e) {
      errors.rejectValue(fieldName, FieldValidationErrorCodes.INVALID.errorCode(fieldName),
          StringUtils.capitalize(displayPrefix) + " must be a valid date in the format dd/mm/yyyy");
      return false;
    }

  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   * Ensures that the date is valid, and the date is on or after the date passed in.
   *
   * @param fieldName     The name of the field on the form for the error to bind to.
   * @param displayName The grouped name in the error message. EG: "proposed start".
   * @param dateStr       The String date to parse, must be in the format (dd/mm/yyyy) to be successfully parsed.
   * @param comparisonDate Date we want to compare with.
   * @param errorSuffix   Text to append to error message.
   * @param errors        Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDatePickerDateIsOnOrAfterComparisonDate(String fieldName,
                                                                        String displayName,
                                                                        String dateStr,
                                                                        LocalDate comparisonDate,
                                                                        String errorSuffix,
                                                                        Errors errors) {
    displayName = displayName.toLowerCase();
    try {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, REQUIRED.errorCode(fieldName), "Enter a " + displayName);

      if (dateStr != null) {
        var date = DateUtils.datePickerStringToDate(dateStr);
        if (date.isBefore(comparisonDate)) {
          errors.rejectValue(fieldName, FieldValidationErrorCodes.BEFORE_SOME_DATE.errorCode(fieldName),
              String.format("%s must be on or after %s", StringUtils.capitalize(displayName), errorSuffix));
          return false;
        }
        return true;
      }

      return false;

    } catch (DateTimeParseException e) {
      errors.rejectValue(fieldName, FieldValidationErrorCodes.INVALID.errorCode(fieldName),
          StringUtils.capitalize(displayName) + " must be a valid date in the format dd/mm/yyyy");
      return false;
    }

  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   * Ensures that the date is valid, and the date is on or after the date passed in.
   * @param fieldPrefix   The prefix of the form date fields. EG: proposedStartDay has a prefix of proposedStart.
   * @param displayPrefix The grouped name in the error message. EG: "proposed start".
   * @param day           Form field day
   * @param month         Form field month
   * @param year          Form field year
   * @param comparisonDate Date to compare with
   * @param errorSuffix Text to append to error message
   * @param errors        Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDateIsOnOrAfterComparisonDate(String fieldPrefix,
                                                              String displayPrefix,
                                                              Integer day,
                                                              Integer month,
                                                              Integer year,
                                                              LocalDate comparisonDate,
                                                              String errorSuffix,
                                                              Errors errors) {
    if (validateDate(fieldPrefix, displayPrefix, day, month, year, errors)) {
      var date = LocalDate.of(year, month, day);
      if (date.isBefore(comparisonDate)) {
        errors.rejectValue(fieldPrefix + "Day",
            String.format("%sDay%s", fieldPrefix, FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode()),
            String.format("%s must not be %s", StringUtils.capitalize(displayPrefix), errorSuffix));
        errors.rejectValue(fieldPrefix + "Month",
            String.format("%sMonth%s", fieldPrefix, FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode()), "");
        errors.rejectValue(fieldPrefix + "Year",
            String.format("%sYear%s", fieldPrefix, FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode()), "");
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
    return validateDateIsOnOrBeforeComparisonDate(
        fieldPrefix,
        displayPrefix,
        day,
        month,
        year,
        LocalDate.now(),
        "in the future",
        errors
    );
  }

  /**
   * Provide standardised error messages to ensure consistent date validation.
   * Ensures that the date is valid, and the date is on or before the date passed in.
   * @param fieldPrefix   The prefix of the form date fields. EG: proposedStartDay has a prefix of proposedStart.
   * @param displayPrefix The grouped name in the error message. EG: "proposed start".
   * @param day           Form field day
   * @param month         Form field month
   * @param year          Form field year
   * @param comparisonDate Date to compare with
   * @param errorSuffix Text to append to error message
   * @param errors        Errors object to add rejection codes and messages to.
   * @return True if date is valid with no errors.
   */
  public static boolean validateDateIsOnOrBeforeComparisonDate(String fieldPrefix,
                                                               String displayPrefix,
                                                               Integer day,
                                                               Integer month,
                                                               Integer year,
                                                               LocalDate comparisonDate,
                                                               String errorSuffix,
                                                               Errors errors) {
    if (validateDate(fieldPrefix, displayPrefix, day, month, year, errors)) {
      var date = LocalDate.of(year, month, day);
      if (date.isAfter(comparisonDate)) {
        errors.rejectValue(fieldPrefix + "Day",
            String.format("%sDay%s", fieldPrefix, FieldValidationErrorCodes.AFTER_SOME_DATE.getCode()),
            String.format("%s must not be %s", StringUtils.capitalize(displayPrefix), errorSuffix));
        errors.rejectValue(fieldPrefix + "Month",
            String.format("%sMonth%s", fieldPrefix, FieldValidationErrorCodes.AFTER_SOME_DATE.getCode()), "");
        errors.rejectValue(fieldPrefix + "Year",
            String.format("%sYear%s", fieldPrefix, FieldValidationErrorCodes.AFTER_SOME_DATE.getCode()), "");
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

    validateMaxLength(errors, field, stringSupplier.get(), messagePrefix + MAX_DEFAULT_STRING_LENGTH_MESSAGE, MAX_DEFAULT_STRING_LENGTH);

  }

  public static void validateMaxStringLength(Errors errors,
                                          String field,
                                          Supplier<String> stringSupplier,
                                          String messagePrefix,
                                          int maxLength) {

    validateMaxLength(errors, field, stringSupplier.get(), messagePrefix + String.format(" must be %s characters or fewer", maxLength),
        maxLength);

  }

  private static void validateMaxLength(Errors errors,
                                        String fieldName,
                                        String testString,
                                        String message,
                                        int maxLength) {
    if (testString != null && testString.length() > maxLength) {
      errors.rejectValue(fieldName, fieldName + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode(), message);
    }
  }

  public static void validateEmailIfPresent(Errors errors, String field, Supplier<String> email, String messagePrefix) {
    var emailAddress = email.get();
    if (emailAddress != null && !EmailValidator.getInstance().isValid(emailAddress)) {
      errors.rejectValue(field, field + FieldValidationErrorCodes.INVALID.getCode(),
          messagePrefix + " must be a valid email");
    }
  }

  /**
   * Ensures that a Boolean object is true.
   *
   * @param errors       Errors object
   * @param field        The field name matching the form's field name.
   * @param errorMessage The message to display if invalid.
   */
  public static void validateBooleanTrue(Errors errors, Boolean bool, String field, String errorMessage) {
    if (!BooleanUtils.toBooleanDefaultIfNull(bool, false)) {
      errors.rejectValue(field, field + ".required", errorMessage);
    }
  }

  public static void validateLatitude(Errors errors,
                                      String errorMessagePrefix,
                                      Pair<String, Integer> degrees,
                                      Pair<String, Integer> minutes,
                                      Pair<String, BigDecimal> seconds) {

    if (degrees.getValue() == null || minutes.getValue() == null || seconds.getValue() == null) {
      errors.rejectValue(degrees.getKey(), degrees.getKey() + REQUIRED.getCode(),
          String.format("Enter the %s latitude details", errorMessagePrefix.toLowerCase()));
      errors.rejectValue(minutes.getKey(), minutes.getKey() + REQUIRED.getCode(), "");
      errors.rejectValue(seconds.getKey(), seconds.getKey() + REQUIRED.getCode(), "");
      return;
    }

    var messagePrefix = errorMessagePrefix + " latitude";

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

    if (!((seconds.getValue().compareTo(BigDecimal.ZERO) >= 0) && (seconds.getValue().compareTo(
        BigDecimal.valueOf(60)) < 0))) {
      errors.rejectValue(seconds.getKey(), seconds.getKey() + INVALID.getCode(),
          String.format("%s seconds should be between 0 and 59.99", messagePrefix));
    }

    if (PwaNumberUtils.getNumberOfDpIncludingTrailingZeros(seconds.getValue()) != CoordinateUtils.DECIMAL_SECONDS_DP) {
      errors.rejectValue(seconds.getKey(), seconds.getKey() + INVALID.getCode(),
          String.format("%s seconds should have exactly %sdp", messagePrefix, CoordinateUtils.DECIMAL_SECONDS_DP));
    }

  }

  public static void validateLongitude(Errors errors,
                                       String errorMessagePrefix,
                                       Pair<String, Integer> degrees,
                                       Pair<String, Integer> minutes,
                                       Pair<String, BigDecimal> seconds,
                                       Pair<String, LongitudeDirection> direction) {

    if (degrees.getValue() == null || minutes.getValue() == null || seconds.getValue() == null || direction.getValue() == null) {
      errors.rejectValue(degrees.getKey(), degrees.getKey() + REQUIRED.getCode(),
          String.format("Enter the %s longitude details", errorMessagePrefix.toLowerCase()));
      errors.rejectValue(minutes.getKey(), minutes.getKey() + REQUIRED.getCode(), "");
      errors.rejectValue(seconds.getKey(), seconds.getKey() + REQUIRED.getCode(), "");
      errors.rejectValue(direction.getKey(), direction.getKey() + REQUIRED.getCode(), "");
      return;
    }

    var messagePrefix = errorMessagePrefix + " longitude";

    if (!Range.between(0, 30).contains(degrees.getValue())) {
      errors.rejectValue(degrees.getKey(), degrees.getKey() + INVALID.getCode(),
          String.format("%s degrees should be between 0 and 30", messagePrefix));
    }

    validateMinutes(errors, minutes, messagePrefix);
    validateSeconds(errors, seconds, messagePrefix);

  }

}
