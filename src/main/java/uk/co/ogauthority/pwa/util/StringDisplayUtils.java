package uk.co.ogauthority.pwa.util;

public class StringDisplayUtils {

  /**
   * Appends "s" to the end of a string if count is not equal to 1.
   *
   * @param str   The string to pluralise.
   * @param count The number of occurrences.
   * @return The pluralised string.
   */
  public static String pluralise(String str, int count) {
    return count != 1 ? str + "s" : str;
  }

}
