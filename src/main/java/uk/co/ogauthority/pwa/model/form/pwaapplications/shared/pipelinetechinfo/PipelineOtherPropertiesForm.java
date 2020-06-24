package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


import java.util.HashMap;
import java.util.Map;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;

public class PipelineOtherPropertiesForm {

  private Map<OtherPipelineProperty, PipelineOtherPropertiesDataForm> propertyDataFormMap = new HashMap<>();


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


}
