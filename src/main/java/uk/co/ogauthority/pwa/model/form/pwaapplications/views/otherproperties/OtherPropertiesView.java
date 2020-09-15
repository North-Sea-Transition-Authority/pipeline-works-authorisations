package uk.co.ogauthority.pwa.model.form.pwaapplications.views.otherproperties;

import java.util.Map;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;

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
