package uk.co.ogauthority.pwa.util.forminputs.decimal;

import java.util.Objects;

public final class DecimalPlaceHint {

  private final int maxDp;

  public DecimalPlaceHint(int maxDp) {
    this.maxDp = maxDp;
  }


  int getMaxDp() {
    return maxDp;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DecimalPlaceHint that = (DecimalPlaceHint) o;
    return maxDp == that.maxDp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxDp);
  }
}
