package uk.co.ogauthority.pwa.model.form.pwaapplications.views.otherproperties;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;

public class OtherPropertiesValueView {

  private final PropertyAvailabilityOption propertyAvailabilityOption;
  private final String minValue;
  private final String maxValue;


  public OtherPropertiesValueView(
      PropertyAvailabilityOption propertyAvailabilityOption, String minValue, String maxValue) {
    this.propertyAvailabilityOption = propertyAvailabilityOption;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  public PropertyAvailabilityOption getPropertyAvailabilityOption() {
    return propertyAvailabilityOption;
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
    OtherPropertiesValueView that = (OtherPropertiesValueView) o;
    return propertyAvailabilityOption == that.propertyAvailabilityOption
        && Objects.equals(minValue, that.minValue)
        && Objects.equals(maxValue, that.maxValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(propertyAvailabilityOption, minValue, maxValue);
  }
}
