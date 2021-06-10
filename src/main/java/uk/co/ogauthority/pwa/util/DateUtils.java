package uk.co.ogauthority.pwa.util;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

public class DateUtils {

  private DateUtils() {
    throw new AssertionError();
  }

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")
      .withLocale(Locale.UK)
      .withZone(ZoneId.systemDefault());

  private static final DateTimeFormatter TWO_DIGIT_YEAR_FORMATTER = DateTimeFormatter.ofPattern("yy");

  public static String formatDate(LocalDate localDate) {
    return localDate.format(DATE_FORMATTER);
  }

  public static String formatDate(Instant instant) {
    return formatDate(instantToLocalDate(instant));
  }

  public static LocalDate instantToLocalDate(Instant instant) {
    return LocalDate.ofInstant(instant, ZoneId.systemDefault());
  }

  public static String createDateEstimateString(int month, int year) {
    return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
  }

  public static String formatDateTime(Instant instant) {
    return DATE_TIME_FORMATTER.format(instant);
  }

  public static String getTwoDigitYear(LocalDate localDate) {
    return localDate.format(TWO_DIGIT_YEAR_FORMATTER);
  }

  public static void consumeInstantFromIntegersElseNull(Integer year,
                                                        Integer month,
                                                        Integer day,
                                                        Consumer<Instant> consumer) {
    try {
      var localDate = LocalDate.of(year, month, day);
      var instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      consumer.accept(instant);
    } catch (DateTimeException | NullPointerException e) {
      consumer.accept(null);
    }
  }

  public static void setYearMonthDayFromInstant(Consumer<Integer> yearConsumer,
                                                Consumer<Integer> monthConsumer,
                                                Consumer<Integer> dayConsumer,
                                                Instant instant) {
    if (instant == null) {
      yearConsumer.accept(null);
      monthConsumer.accept(null);
      dayConsumer.accept(null);
    } else {
      var localDate = LocalDate.ofInstant(instant, ZoneId.systemDefault());
      yearConsumer.accept(localDate.getYear());
      monthConsumer.accept(localDate.getMonthValue());
      dayConsumer.accept(localDate.getDayOfMonth());
    }
  }

  public static LocalDate datePickerStringToDate(String dateStr) {
    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
  }

  public static String formatToDatePickerString(LocalDate date) {
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
  }

}
