package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import java.util.Map;
import java.util.Set;

public class OtherPropertiesView {

  private final Map<OtherPipelineProperty, OtherPropertiesValueView> propertyValueMap;
  private final Set<PropertyPhase> selectedPropertyPhases;
  private final String otherPhaseDescription;

  public OtherPropertiesView(
      Map<OtherPipelineProperty, OtherPropertiesValueView> propertyValueMap,
      Set<PropertyPhase> selectedPropertyPhases, String otherPhaseDescription) {
    this.propertyValueMap = propertyValueMap;
    this.selectedPropertyPhases = selectedPropertyPhases;
    this.otherPhaseDescription = otherPhaseDescription;
  }

  public Map<OtherPipelineProperty, OtherPropertiesValueView> getPropertyValueMap() {
    return propertyValueMap;
  }

  public Set<PropertyPhase> getSelectedPropertyPhases() {
    return selectedPropertyPhases;
  }

  public String getOtherPhaseDescription() {
    return otherPhaseDescription;
  }



}
