package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

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
