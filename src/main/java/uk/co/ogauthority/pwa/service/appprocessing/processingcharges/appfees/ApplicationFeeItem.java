package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees;


import java.util.Objects;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.PaymentItem;

/**
 * Object that describes a single item within the breakdown of total fee.
 */
public final class ApplicationFeeItem implements PaymentItem {

  private final String description;
  private final int pennyAmount;

  ApplicationFeeItem(String description, int pennyAmount) {
    this.description = description;
    this.pennyAmount = pennyAmount;
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
    ApplicationFeeItem that = (ApplicationFeeItem) o;
    return pennyAmount == that.pennyAmount && Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, pennyAmount);
  }
}
