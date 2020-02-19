package uk.co.ogauthority.pwa.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

  public static String formatDate(LocalDate localDate) {
    var dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM YYYY");
    return localDate.format(dateTimeFormatter);
  }

}
