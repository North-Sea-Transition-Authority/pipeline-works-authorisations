package uk.co.ogauthority.pwa.util;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class DateUtils {

  public DateUtils() {
    throw new AssertionError();
  }

  private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM YYYY");

  public static String formatDate(LocalDate localDate) {
    return localDate.format(dateTimeFormatter);
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

}
