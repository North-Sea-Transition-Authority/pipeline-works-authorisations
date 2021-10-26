package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;


import java.util.Objects;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;

public class PipelineOtherPropertiesDataForm {

  private PropertyAvailabilityOption propertyAvailabilityOption;
  private MinMaxInput minMaxInput;


  public PropertyAvailabilityOption getPropertyAvailabilityOption() {
    return propertyAvailabilityOption;
  }

  public void setPropertyAvailabilityOption(
      PropertyAvailabilityOption propertyAvailabilityOption) {
    this.propertyAvailabilityOption = propertyAvailabilityOption;
  }

  public MinMaxInput getMinMaxInput() {
    return minMaxInput;
  }

  public void setMinMaxInput(MinMaxInput minMaxInput) {
    this.minMaxInput = minMaxInput;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineOtherPropertiesDataForm that = (PipelineOtherPropertiesDataForm) o;
    return propertyAvailabilityOption == that.propertyAvailabilityOption
        && Objects.equals(minMaxInput, that.minMaxInput);
  }

  @Override
  public int hashCode() {
    return Objects.hash(propertyAvailabilityOption, minMaxInput);
  }
}
