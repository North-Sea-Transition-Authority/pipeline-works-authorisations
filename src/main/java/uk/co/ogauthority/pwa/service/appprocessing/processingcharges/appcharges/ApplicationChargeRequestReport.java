package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.PaymentHeader;

/**
 * Class to capture the breakdown of an application submission fee.
 */
public final class ApplicationChargeRequestReport implements PaymentHeader<ApplicationChargeItem> {

  private final PwaApplication pwaApplication;
  private final int totalPennies;
  private final String feeSummary;
  private final List<ApplicationChargeItem> applicationChargeItems;
  private final PwaAppChargeRequestStatus pwaAppChargeRequestStatus;
  private final String waivedReason;


  ApplicationChargeRequestReport(PwaApplication pwaApplication,
                                 Integer totalPennies,
                                 String feeSummary,
                                 List<ApplicationChargeItem> applicationChargeItems,
                                 PwaAppChargeRequestStatus pwaAppChargeRequestStatus,
                                 String waivedReason) {
    this.pwaApplication = pwaApplication;
    this.totalPennies = totalPennies;
    this.feeSummary = feeSummary;
    this.applicationChargeItems = applicationChargeItems;
    this.pwaAppChargeRequestStatus = pwaAppChargeRequestStatus;
    this.waivedReason = waivedReason;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }


  @Override
  public int getTotalPennies() {
    return totalPennies;
  }

  @Override
  public String getSummary() {
    return feeSummary;
  }

  @Override
  public List<ApplicationChargeItem> getPaymentItems() {
    return Collections.unmodifiableList(applicationChargeItems);
  }

  public PwaAppChargeRequestStatus getPwaAppChargeRequestStatus() {
    return pwaAppChargeRequestStatus;
  }

  public String getWaivedReason() {
    return waivedReason;
  }
}
