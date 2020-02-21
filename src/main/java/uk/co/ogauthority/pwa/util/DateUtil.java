package uk.co.ogauthority.pwa.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

  private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM YYYY");

  public static String formatDate(LocalDate localDate) {
    return localDate.format(dateTimeFormatter);
  }

}
