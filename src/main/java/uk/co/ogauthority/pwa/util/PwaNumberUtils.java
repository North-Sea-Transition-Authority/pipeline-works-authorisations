package uk.co.ogauthority.pwa.util;

import java.math.BigDecimal;

public class PwaNumberUtils {

  private PwaNumberUtils() {
    throw new AssertionError();
  }

  public static int getNumberOfDp(BigDecimal value) {
    return value.scale();
  }

  public static boolean numberOfDecimalPlacesLessThanOrEqual(BigDecimal value,
                                                             int maxDecimalPlaces,
                                                             boolean outputWhenNull) {
    if (value != null) {
      return value.scale() <= maxDecimalPlaces;
    }

    return outputWhenNull;

  }

}
