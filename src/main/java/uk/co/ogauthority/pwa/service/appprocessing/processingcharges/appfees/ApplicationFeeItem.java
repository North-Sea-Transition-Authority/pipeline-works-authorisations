package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees;


import java.util.Objects;

/**
 * Object that describes a single item within the breakdown of total fee.
 */
public final class ApplicationFeeItem {

  private final String description;
  private final int pennyAmount;

  ApplicationFeeItem(String description, int pennyAmount) {
    this.description = description;
    this.pennyAmount = pennyAmount;
  }

  public String getDescription() {
    return description;
  }

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
