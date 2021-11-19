package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;


import java.util.Objects;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.PaymentItem;

/**
 * Object that describes a single item within a charge request.
 */
public final class ApplicationChargeItem implements PaymentItem {

  private final String description;
  private final int pennyAmount;

  ApplicationChargeItem(String description, int pennyAmount) {
    this.description = description;
    this.pennyAmount = pennyAmount;
  }

  static ApplicationChargeItem from(PwaAppChargeRequestItem pwaAppChargeRequestItem) {
    return new ApplicationChargeItem(
        pwaAppChargeRequestItem.getDescription(),
        pwaAppChargeRequestItem.getPennyAmount()
    );
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public int getPennyAmount() {
    return pennyAmount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationChargeItem that = (ApplicationChargeItem) o;
    return pennyAmount == that.pennyAmount && Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, pennyAmount);
  }
}
