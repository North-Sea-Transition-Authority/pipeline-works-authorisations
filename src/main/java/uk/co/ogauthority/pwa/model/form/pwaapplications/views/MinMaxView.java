package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;

public class MinMaxView {

  private final String minPrompt;
  private final String maxPrompt;
  private final UnitMeasurement unitMeasurement;

  private final String minValue;
  private final String maxValue;


  public MinMaxView(String minPrompt, String maxPrompt, UnitMeasurement unitMeasurement, String minValue,
                    String maxValue) {
    this.minPrompt = minPrompt;
    this.maxPrompt = maxPrompt;
    this.unitMeasurement = unitMeasurement;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  // common view constructors
  public static MinMaxView createInternalExternalView(String minValue, String maxValue, UnitMeasurement unit) {
    return new MinMaxView(
        "internal", "external", unit, minValue, maxValue
    );
  }

  public static MinMaxView createMinMaxView(String minValue, String maxValue, UnitMeasurement unit) {
    return new MinMaxView(
        "min", "max", unit, minValue, maxValue
    );
  }


  public String getMinPrompt() {
    return minPrompt;
  }

  public String getMaxPrompt() {
    return maxPrompt;
  }

  public UnitMeasurement getUnitMeasurement() {
    return unitMeasurement;
  }

  public String getMinValue() {
    return minValue;
  }

  public String getMaxValue() {
    return maxValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MinMaxView that = (MinMaxView) o;
    return Objects.equals(minPrompt, that.minPrompt)
        && Objects.equals(maxPrompt, that.maxPrompt)
        && unitMeasurement == that.unitMeasurement
        && Objects.equals(minValue, that.minValue)
        && Objects.equals(maxValue, that.maxValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(minPrompt, maxPrompt, unitMeasurement, minValue, maxValue);
  }
}
