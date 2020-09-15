package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.Objects;

public class PairValueView {

  private final String valueOne;
  private final String valueTwo;

  public PairValueView(String valueOne, String valueTwo) {
    this.valueOne = valueOne;
    this.valueTwo = valueTwo;
  }

  public String getValueOne() {
    return valueOne;
  }

  public String getValueTwo() {
    return valueTwo;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PairValueView that = (PairValueView) o;
    return Objects.equals(valueOne, that.valueOne)
        && Objects.equals(valueTwo, that.valueTwo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(valueOne, valueTwo);
  }

}
