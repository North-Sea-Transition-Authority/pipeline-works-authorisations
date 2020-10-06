package uk.co.ogauthority.pwa.util;

import java.math.BigDecimal;

public class NumberUtils {

  private NumberUtils() {
    throw new AssertionError();
  }

  public static int getNumberOfDp(BigDecimal value) {
    return value.scale();
  }

}
