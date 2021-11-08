package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees;

import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.feeproviders.ApplicationFeeItem;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.PaymentHeader;

/**
 * Class to capture the breakdown of an application submission fee.
 */
public final class ApplicationFeeReport implements PaymentHeader<ApplicationFeeItem> {

  private final PwaApplication pwaApplication;
  private final Integer totalPennies;
  private final String feeSummary;
  private final List<ApplicationFeeItem> applicationFeeItems;


  ApplicationFeeReport(PwaApplication pwaApplication,
                       Integer totalPennies,
                       String feeSummary,
                       List<ApplicationFeeItem> applicationFeeItems) {
    this.pwaApplication = pwaApplication;
    this.totalPennies = totalPennies;
    this.feeSummary = feeSummary;
    this.applicationFeeItems = applicationFeeItems;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  @Override
  public String getSummary() {
    return feeSummary;
  }

  @Override
  public List<ApplicationFeeItem> getPaymentItems() {
    return Collections.unmodifiableList(applicationFeeItems);
  }

  @Override
  public int getTotalPennies() {
    return totalPennies;
  }

}
