package uk.co.ogauthority.pwa.util.forminputs.decimal;

import java.math.BigDecimal;
import java.util.Objects;

public class GreaterThanEqualToHint {
  private final BigDecimal smallerNumber;

  public GreaterThanEqualToHint(BigDecimal smallerNumber) {
    this.smallerNumber = smallerNumber;
  }


  BigDecimal getSmallerNumber() {
    return smallerNumber;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GreaterThanEqualToHint that = (GreaterThanEqualToHint) o;
    return Objects.equals(smallerNumber, that.smallerNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(smallerNumber);
  }
}

