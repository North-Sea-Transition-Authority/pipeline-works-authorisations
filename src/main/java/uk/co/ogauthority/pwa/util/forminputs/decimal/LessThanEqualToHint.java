package uk.co.ogauthority.pwa.util.forminputs.decimal;

import java.math.BigDecimal;
import java.util.Objects;

public final class LessThanEqualToHint {

  private final BigDecimal largerNumber;

  public LessThanEqualToHint(BigDecimal largerNumber) {
    this.largerNumber = largerNumber;
  }


  BigDecimal getLargerNumber() {
    return largerNumber;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LessThanEqualToHint that = (LessThanEqualToHint) o;
    return Objects.equals(largerNumber, that.largerNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(largerNumber);
  }
}
