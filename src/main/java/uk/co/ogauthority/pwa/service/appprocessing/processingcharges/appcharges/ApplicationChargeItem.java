package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;


/**
 * Object that describes a single item within a charge request.
 */
public final class ApplicationChargeItem {

  private final String description;
  private final int pennyAmount;

  ApplicationChargeItem(String description, int pennyAmount) {
    this.description = description;
    this.pennyAmount = pennyAmount;
  }

  public String getDescription() {
    return description;
  }

  public int getPennyAmount() {
    return pennyAmount;
  }
}
