package uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges;

/**
 * Defines the statue of a charge request made on an application.
 */
public enum PwaAppChargeRequestStatus {
  OPEN("Open"),
  WAIVED("Waived"),
  PAID("Paid"),
  CANCELLED("Cancelled");

  private final String dispayString;

  PwaAppChargeRequestStatus(String dispayString) {
    this.dispayString = dispayString;
  }

  public String getDispayString() {
    return dispayString;
  }
}
