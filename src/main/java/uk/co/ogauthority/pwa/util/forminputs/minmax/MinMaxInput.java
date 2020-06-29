package uk.co.ogauthority.pwa.util.forminputs.minmax;

import java.math.BigDecimal;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents minimum and maximum fields commonly used on forms and provides access to common operations
 * that might be applied to those values.
 * e.g testing if a given value is smaller that the other etc.
 */
public class MinMaxInput {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      MinMaxInput.class);


  private BigDecimal minValue;
  private BigDecimal maxValue;

  public MinMaxInput() { // default constructor required by hibernate
  }

  public MinMaxInput(BigDecimal minValue, BigDecimal maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
  }


  //Utils
  public boolean isMinEmpty() {
    return minValue == null;
  }

  public boolean isMaxEmpty() {
    return maxValue == null;
  }


  public boolean minSmallerThanMax() {
    return minValue.compareTo(maxValue) == -1;
  }

  public boolean minSmallerOrEqualToMax() {
    int comparedResult = minValue.compareTo(maxValue);
    return comparedResult == -1 || comparedResult == 0;
  }


  public boolean minHasValidDecimalPlaces(int maxDecimalPlaces) {
    return Math.max(0, minValue.stripTrailingZeros().scale()) <= maxDecimalPlaces;
  }

  public boolean maxHasValidDecimalPlaces(int maxDecimalPlaces) {
    return Math.max(0, maxValue.stripTrailingZeros().scale()) <= maxDecimalPlaces;
  }


  public boolean isMinPositive() {
    return minValue.compareTo(BigDecimal.ZERO) == 1;
  }

  public boolean isMaxPositive() {
    return maxValue.compareTo(BigDecimal.ZERO) == 1;
  }

  public boolean isMinInteger() {
    return minValue.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
  }

  public boolean isMaxInteger() {
    return maxValue.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
  }





  //Getters/Setters
  public BigDecimal getMinValue() {
    return minValue;
  }

  public void setMinValue(BigDecimal minValue) {
    this.minValue = minValue;
  }

  public BigDecimal getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(BigDecimal maxValue) {
    this.maxValue = maxValue;
  }






  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MinMaxInput that = (MinMaxInput) o;
    return Objects.equals(minValue, that.minValue)
        && Objects.equals(maxValue, that.maxValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(minValue, maxValue);
  }
}
