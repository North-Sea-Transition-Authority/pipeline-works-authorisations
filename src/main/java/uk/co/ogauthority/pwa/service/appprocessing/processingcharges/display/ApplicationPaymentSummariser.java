package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display;

import java.text.DecimalFormat;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Converts Payment Header and PaymentItem representations so they can be consumed by the frontend.
 */
@Service
public class ApplicationPaymentSummariser {

  private static final DecimalFormat GBP_FORMAT = new DecimalFormat("0.00");

  public <T extends PaymentItem> ApplicationPaymentDisplaySummary summarise(PaymentHeader<T> paymentHeader) {

    return new ApplicationPaymentDisplaySummary(
        paymentHeader.getSummary(),
        formatPennies(paymentHeader.getTotalPennies()),
        paymentHeader.getPaymentItems().stream()
            .map(this::createDisplayableFeeItem)
            .collect(Collectors.toUnmodifiableList())
    );
  }

  private DisplayableFeeItem createDisplayableFeeItem(PaymentItem paymentItem) {
    return new DisplayableFeeItem(paymentItem.getDescription(), formatPennies(paymentItem.getPennyAmount()));
  }

  private String formatPennies(int pennies) {
    return GBP_FORMAT.format(pennies / 100);
  }

}
