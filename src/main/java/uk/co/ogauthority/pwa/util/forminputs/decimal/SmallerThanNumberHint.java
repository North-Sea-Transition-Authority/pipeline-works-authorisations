package uk.co.ogauthority.pwa.util.forminputs.decimal;

import java.math.BigDecimal;
import java.util.Objects;

public final class SmallerThanNumberHint {

  private final BigDecimal largerNumber;
  private final String formInputLabel;

  public SmallerThanNumberHint(BigDecimal largerNumber, String formInputLabel) {
    this.largerNumber = largerNumber;
    this.formInputLabel = formInputLabel;
  }


  BigDecimal getLargerNumber() {
    return largerNumber;
  }

  String getFormInputLabel() {
    return formInputLabel;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SmallerThanNumberHint that = (SmallerThanNumberHint) o;
    return Objects.equals(largerNumber, that.largerNumber)
        && Objects.equals(formInputLabel, that.formInputLabel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(largerNumber, formInputLabel);
  }
}
