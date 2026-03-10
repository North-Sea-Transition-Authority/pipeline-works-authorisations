package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders;


import java.util.Objects;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.PaymentItem;

/**
 * Object that describes a single item within the breakdown of total fee.
 */
public final class ApplicationFeeItem implements PaymentItem {

  private final PwaApplicationFeeType pwaApplicationFeeType;
  private final String description;
  private final int pennyAmount;

  ApplicationFeeItem(PwaApplicationFeeType pwaApplicationFeeType, String description, int pennyAmount) {
    this.pwaApplicationFeeType = pwaApplicationFeeType;
    this.description = description;
    this.pennyAmount = pennyAmount;
  }

  @Override
  public PwaApplicationFeeType getPwaApplicationFeeType() {
    return pwaApplicationFeeType;
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
    return pwaApplicationFeeType == that.pwaApplicationFeeType
        && pennyAmount == that.pennyAmount
        && Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pwaApplicationFeeType, description, pennyAmount);
  }
}
