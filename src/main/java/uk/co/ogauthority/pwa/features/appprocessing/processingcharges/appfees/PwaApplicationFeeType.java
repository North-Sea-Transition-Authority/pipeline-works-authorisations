package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees;

/**
 * Define the various different types of fee that can be applied to applications.
 */
public enum PwaApplicationFeeType {
  DEFAULT("Application fee"),
  FAST_TRACK("Fast-track surcharge");

  PwaApplicationFeeType(String displayName) {
    this.displayName = displayName;
  }

  private String displayName;

  public String getDisplayName() {
    return displayName;
  }
}
