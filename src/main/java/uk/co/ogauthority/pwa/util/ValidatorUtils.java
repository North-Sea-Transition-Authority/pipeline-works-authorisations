package uk.co.ogauthority.pwa.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;

public class ValidatorUtils {

  public ValidatorUtils() {
    throw new AssertionError();
  }

  public static boolean isIntegerBetween(Integer num, int minInclusive, int maxInclusive) {
    return num != null && num >= minInclusive && num <= maxInclusive;
  }

  public static void validateDate(String fieldPrefix, String displayPrefix,
                                     Integer day, Integer month, Integer year, Errors errors) {
    var dayValid = isIntegerBetween(day, 1, 31);
    var monthValid = isIntegerBetween(month, 1, 12);
    var yearValid = year != null && year >= LocalDate.now().getYear();
    if (dayValid && monthValid && yearValid) {
      try {
        var date = LocalDate.of(year, month, day);
        if (date.isBefore(LocalDate.now())) {
          errors.rejectValue(fieldPrefix + "Day", String.format("%sDay.beforeToday", fieldPrefix),
              String.format("%s must not be in the past", StringUtils.capitalize(displayPrefix)));
          errors.rejectValue(fieldPrefix + "Month", String.format("%sMonth.beforeToday", fieldPrefix), "");
          errors.rejectValue(fieldPrefix + "Year", String.format("%sYear.beforeToday", fieldPrefix), "");
        }
      } catch (DateTimeException dte) {
        errors.rejectValue(fieldPrefix + "Day", String.format("%sDay.invalid", fieldPrefix),
            String.format("Enter a valid %s day", displayPrefix));
        errors.rejectValue(fieldPrefix + "Month", String.format("%sMonth.invalid", fieldPrefix), "");
        errors.rejectValue(fieldPrefix + "Year", String.format("%sYear.invalid", fieldPrefix), "");
      }
    } else {
      errors.rejectValue(fieldPrefix + "Day", String.format("%sDay.invalid", fieldPrefix),
          String.format("Enter a valid %s date", displayPrefix));
      errors.rejectValue(fieldPrefix + "Month", String.format("%sMonth.invalid", fieldPrefix), "");
      errors.rejectValue(fieldPrefix + "Year", String.format("%sYear.invalid", fieldPrefix), "");
    }
  }

}
