package uk.co.ogauthority.pwa.util.validation.minmax;

public class MinMaxLimit {

  private Integer minValue;

  private Integer maxValue;

  public MinMaxLimit(Integer minValue, Integer maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  public Integer getMinimumLimit() {
    return minValue;
  }

  public MinMaxLimit setMinValue(Integer minValue) {
    this.minValue = minValue;
    return this;
  }

  public Integer getMaximumLimit() {
    return maxValue;
  }

  public MinMaxLimit setMaxValue(Integer maxValue) {
    this.maxValue = maxValue;
    return this;
  }
}
