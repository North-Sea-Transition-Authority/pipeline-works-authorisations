package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;

public class PipelineOtherPropertiesForm {

  private Map<OtherPipelineProperty, PipelineOtherPropertiesDataForm> propertyDataFormMap = new HashMap<>();
  private Set<PropertyPhase> phasesPresent = new HashSet<>();
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

  public Set<PropertyPhase> getPhasesPresent() {
    return phasesPresent;
  }

  public void setPhasesPresent(
      Set<PropertyPhase> phasesPresent) {
    this.phasesPresent = phasesPresent;
  }

  public void setOtherPhaseDescription(String otherPhaseDescription) {
    this.otherPhaseDescription = otherPhaseDescription;
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
        && Objects.equals(phasesPresent, that.phasesPresent)
        && Objects.equals(otherPhaseDescription, that.otherPhaseDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(propertyDataFormMap, phasesPresent, otherPhaseDescription);
  }
}
