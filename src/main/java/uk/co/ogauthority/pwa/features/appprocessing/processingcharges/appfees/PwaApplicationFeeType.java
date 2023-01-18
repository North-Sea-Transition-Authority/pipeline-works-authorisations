package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees;

/**
 * Define the various different types of fee that can be applied to applications.
 */
public enum PwaApplicationFeeType {

  DEFAULT("Application fee", 10),
  FAST_TRACK("Fast-track surcharge", 20);

  private final String displayName;
  private final int displayOrder;

  PwaApplicationFeeType(String displayName,
                        int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
