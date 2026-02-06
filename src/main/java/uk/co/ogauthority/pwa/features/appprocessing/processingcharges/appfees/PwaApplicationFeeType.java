package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees;

import uk.co.ogauthority.pwa.util.enumutils.Displayable;

/**
 * Define the various different types of fee that can be applied to applications.
 */
public enum PwaApplicationFeeType implements Displayable {

  DEFAULT("Application fee", 10),
  FAST_TRACK("Fast-track surcharge", 20);

  private final String displayName;
  private final int displayOrder;

  PwaApplicationFeeType(String displayName,
                        int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }
}
