package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees;

import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

/**
 * Class to capture the breakdown of an application submission fee.
 */
public final class ApplicationFeeReport {

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

  public Integer getTotalPennies() {
    return totalPennies;
  }

  public String getFeeSummary() {
    return feeSummary;
  }

  public List<ApplicationFeeItem> getApplicationFeeItems() {
    return Collections.unmodifiableList(applicationFeeItems);
  }
}
