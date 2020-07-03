package uk.co.ogauthority.pwa.util.forminputs.minmax;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
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

  private String minValue;
  private String maxValue;

  public MinMaxInput() { // default constructor required by hibernate
  }

  public MinMaxInput(String minValue, String maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
  }


  //Utils
  public boolean isMinNumeric() {
    return createMinOrNull() != null;
  }

  public boolean isMaxNumeric() {
    return createMaxOrNull() != null;
  }


  public boolean minSmallerThanMax() {
    return createMinOrNull().compareTo(createMaxOrNull()) == -1;
  }

  public boolean minSmallerOrEqualToMax() {
    int comparedResult = createMinOrNull().compareTo(createMaxOrNull());
    return comparedResult == -1 || comparedResult == 0;
  }


  public boolean minHasValidDecimalPlaces(int maxDecimalPlaces) {
    return Math.max(0, createMinOrNull().stripTrailingZeros().scale()) <= maxDecimalPlaces;
  }

  public boolean maxHasValidDecimalPlaces(int maxDecimalPlaces) {
    return Math.max(0, createMaxOrNull().stripTrailingZeros().scale()) <= maxDecimalPlaces;
  }


  public boolean isMinPositive() {
    return createMinOrNull().compareTo(BigDecimal.ZERO) == 1;
  }

  public boolean isMaxPositive() {
    return createMaxOrNull().compareTo(BigDecimal.ZERO) == 1;
  }

  public boolean isMinInteger() {
    return createMinOrNull().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
  }

  public boolean isMaxInteger() {
    return createMaxOrNull().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
  }


  //Getters/Setters
  public String getMinValue() {
    return minValue;
  }

  public void setMinValue(String minValue) {
    this.minValue = minValue;
  }

  public String getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
  }


  public BigDecimal createMinOrNull() {
    return this.createBigDecimal(minValue)
        .orElse(null);
  }

  public BigDecimal createMaxOrNull() {
    return this.createBigDecimal(maxValue)
        .orElse(null);
  }

  public Optional<BigDecimal> createBigDecimal(String valueStr) {
    try {
      var createdNum = valueStr != null ? new BigDecimal(valueStr) : null;
      return Optional.ofNullable(createdNum);
    } catch (NumberFormatException e) {
      LOGGER.debug("Could not convert minimum/maximum values to valid numbers. " + this.toString(), e);
      return Optional.empty();
    }
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


