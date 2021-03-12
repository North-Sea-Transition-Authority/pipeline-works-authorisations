package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display;

import java.util.List;

public final class ApplicationPaymentDisplaySummaryTestUtil {

  private ApplicationPaymentDisplaySummaryTestUtil() {
    throw new UnsupportedOperationException("no util for you1!");
  }


  public static ApplicationPaymentDisplaySummary getDefaultPaymentDisplaySummary() {
    return new ApplicationPaymentDisplaySummary(
        "Payment summary headline",
        "1.00",
        List.of(new DisplayableFeeItem("Fee item 1", "1.00"))

    );
  }

  public static ApplicationPaymentDisplaySummary createSimpleSummary(String header, String formattedAmount) {
    return new ApplicationPaymentDisplaySummary(
        header,
        formattedAmount,
        List.of(new DisplayableFeeItem("Fee item 1", "1.00"))

    );
  }


}