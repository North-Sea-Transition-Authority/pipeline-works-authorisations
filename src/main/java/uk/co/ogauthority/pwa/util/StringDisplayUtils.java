package uk.co.ogauthority.pwa.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class StringDisplayUtils {

  private static final DecimalFormat DECIMAL_FORMAT_2DP = new DecimalFormat("#.##");
  private static final DecimalFormat INTEGER_FORMAT_MIN_2_DIGIT_LEAD_ZERO_PADDING = new DecimalFormat("00");

  // not instantiatable
  private StringDisplayUtils() {
    throw new AssertionError();
  }

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

  public static String formatDecimal2DpOrNull(BigDecimal bigDecimal) {
    return bigDecimal != null ? DECIMAL_FORMAT_2DP.format(bigDecimal) : null;
  }

  public static String formatInteger2DigitZeroPaddingOrNull(Integer integer) {
    return integer != null ? INTEGER_FORMAT_MIN_2_DIGIT_LEAD_ZERO_PADDING.format(integer) : null;
  }

  public static String formatDecimal2DpSeparatedSuffixedOrNull(BigDecimal bigDecimal, String suffix) {
    return bigDecimal != null
        ? String.format("%,.2f", bigDecimal.setScale(2, RoundingMode.HALF_UP)) + suffix
        : null;
  }

}
