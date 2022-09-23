package uk.co.ogauthority.pwa.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class CurrencyUtils {

  private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("0.00");

  private CurrencyUtils() {
    throw new AssertionError();
  }

  public static String pennyAmountToCurrency(Integer pennyAmount) {
    Double poundAmount = pennyAmount / 100.00;
    return CURRENCY_FORMAT.format(poundAmount);
  }

  public static Integer currencyToPennyAmount(String currency) {
    var poundAmount = Double.valueOf(currency);
    return Math.toIntExact(Math.round(poundAmount * 100));
  }

  public static boolean isValueCurrency(Double currency) {
    if (currency < 0.0) {
      return false;
    }
    return BigDecimal.valueOf(currency).scale() <= 2;
  }
}
