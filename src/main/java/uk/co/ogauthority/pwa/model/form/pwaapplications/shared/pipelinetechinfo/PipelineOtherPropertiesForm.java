package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;

public class PipelineOtherPropertiesForm {

  private Map<OtherPipelineProperty, PipelineOtherPropertiesDataForm> propertyDataFormMap = new HashMap<>();
  private Map<PropertyPhase, String> phasesSelection = new HashMap<>();
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

  public String getOtherPhaseDescription() {
    return otherPhaseDescription;
  }

  public void setOtherPhaseDescription(String otherPhaseDescription) {
    this.otherPhaseDescription = otherPhaseDescription;
  }

  public Map<PropertyPhase, String> getPhasesSelection() {
    return phasesSelection;
  }

  public void setPhasesSelection(
      Map<PropertyPhase, String> phasesSelection) {
    this.phasesSelection = phasesSelection;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineOtherPropertiesForm that = (PipelineOtherPropertiesForm) o;
    return Objects.equals(propertyDataFormMap, that.propertyDataFormMap)
        && Objects.equals(phasesSelection, that.phasesSelection)
        && Objects.equals(otherPhaseDescription, that.otherPhaseDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(propertyDataFormMap, phasesSelection, otherPhaseDescription);
  }
}
