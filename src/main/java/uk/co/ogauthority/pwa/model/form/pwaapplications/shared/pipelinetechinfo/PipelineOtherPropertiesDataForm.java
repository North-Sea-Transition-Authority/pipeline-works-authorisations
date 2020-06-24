package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;
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
}
