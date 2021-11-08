package uk.co.ogauthority.pwa.util;

import java.math.BigDecimal;

public class PwaNumberUtils {

  private PwaNumberUtils() {
    throw new AssertionError();
  }

  public static int getNumberOfDpIncludingTrailingZeros(BigDecimal value) {
    return value.scale();
  }

  public static int getNumberOfDp(BigDecimal value) {
    //Discussion: https://stackoverflow.com/a/30471139
    return Math.max(0, value.stripTrailingZeros().scale());
  }

  public static boolean numberOfDecimalPlacesLessThanOrEqual(BigDecimal value,
                                                             int maxDecimalPlaces,
                                                             boolean outputWhenNull) {
    if (value == null) {
      return outputWhenNull;
    }

    return getNumberOfDp(value) <= maxDecimalPlaces;

  }

}
