package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


import java.util.HashMap;
import java.util.Map;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;

public class PipelineOtherPropertiesForm {

  private Map<OtherPipelineProperty, PipelineOtherPropertiesDataForm> propertyDataFormMap = new HashMap<>();
  private boolean oilPresent;
  private boolean condensatePresent;
  private boolean gasPresent;
  private boolean waterPresent;
  private boolean otherPresent;
  private String otherPhaseDescription;


  public Map<OtherPipelineProperty, PipelineOtherPropertiesDataForm> getPropertyDataFormMap() {
    return propertyDataFormMap;
  }

  public void setPropertyDataFormMap(
      Map<OtherPipelineProperty, PipelineOtherPropertiesDataForm> propertyDataFormMap) {
    this.propertyDataFormMap = propertyDataFormMap;
  }

  public void addPropertyData(OtherPipelineProperty otherPipelineProperty,
                              PipelineOtherPropertiesDataForm pipelineOtherPropertiesDataForm) {
    propertyDataFormMap.put(otherPipelineProperty, pipelineOtherPropertiesDataForm);
  }


  public boolean getOilPresent() {
    return oilPresent;
  }

  public void setOilPresent(boolean oilPresent) {
    this.oilPresent = oilPresent;
  }

  public boolean getCondensatePresent() {
    return condensatePresent;
  }

  public void setCondensatePresent(boolean condensatePresent) {
    this.condensatePresent = condensatePresent;
  }

  public boolean getGasPresent() {
    return gasPresent;
  }

  public void setGasPresent(boolean gasPresent) {
    this.gasPresent = gasPresent;
  }

  public boolean getWaterPresent() {
    return waterPresent;
  }

  public void setWaterPresent(boolean waterPresent) {
    this.waterPresent = waterPresent;
  }

  public boolean getOtherPresent() {
    return otherPresent;
  }

  public void setOtherPresent(boolean otherPresent) {
    this.otherPresent = otherPresent;
  }

  public String getOtherPhaseDescription() {
    return otherPhaseDescription;
  }

  public void setOtherPhaseDescription(String otherPhaseDescription) {
    this.otherPhaseDescription = otherPhaseDescription;
  }
}
