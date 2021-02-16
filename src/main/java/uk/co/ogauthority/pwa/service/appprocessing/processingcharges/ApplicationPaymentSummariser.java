package uk.co.ogauthority.pwa.service.appprocessing.processingcharges;

import java.text.DecimalFormat;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.ApplicationFeeReport;

/**
 * Converts fee and charge representations so they can be consumed by the frontend.
 */
@Service
public class ApplicationPaymentSummariser {

  private static final DecimalFormat GBP_FORMAT = new DecimalFormat("0.00");

  public ApplicationPaymentDisplaySummary summarise(ApplicationFeeReport applicationFeeReport) {

    return new ApplicationPaymentDisplaySummary(
        applicationFeeReport.getFeeSummary(),
        formatPennies(applicationFeeReport.getTotalPennies()),
        applicationFeeReport.getApplicationFeeItems().stream()
            .map(o -> new DisplayableFeeItem(o.getDescription(), formatPennies(o.getPennyAmount())))
            .collect(Collectors.toUnmodifiableList())
    );
  }

  private String formatPennies(int pennies) {
    return GBP_FORMAT.format(pennies / 100);
  }

}
