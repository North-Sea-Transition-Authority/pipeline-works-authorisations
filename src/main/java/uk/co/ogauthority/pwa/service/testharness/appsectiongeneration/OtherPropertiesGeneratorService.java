package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadPipelineOtherPropertiesService;

@Service
@Profile("development")
public class OtherPropertiesGeneratorService {

  private final PadPipelineOtherPropertiesService otherPropertiesService;


  @Autowired
  public OtherPropertiesGeneratorService(
      PadPipelineOtherPropertiesService otherPropertiesService) {
    this.otherPropertiesService = otherPropertiesService;
  }


  public void generateOtherProperties(PwaApplicationDetail pwaApplicationDetail) {

    var form = createOtherPropertiesForm();
    otherPropertiesService.saveEntitiesUsingForm(
        form, otherPropertiesService.getPipelineOtherPropertyEntities(pwaApplicationDetail), pwaApplicationDetail);
  }


  private PipelineOtherPropertiesForm createOtherPropertiesForm() {

    Map<OtherPipelineProperty, PipelineOtherPropertiesDataForm> propertyDataFormMap = new HashMap<>();
    OtherPipelineProperty.asList().forEach(property -> {
      var dataForm = new PipelineOtherPropertiesDataForm();
      dataForm.setPropertyAvailabilityOption(PropertyAvailabilityOption.NOT_PRESENT);
      propertyDataFormMap.put(property, dataForm);
    });

    Map<PropertyPhase, String> phasesSelectionMap = new HashMap<>();
    PropertyPhase.asList().forEach(phase -> phasesSelectionMap.put(phase, ""));

    var pipelineOtherPropertiesForm = new PipelineOtherPropertiesForm();
    pipelineOtherPropertiesForm.setPropertyDataFormMap(propertyDataFormMap);
    pipelineOtherPropertiesForm.setPhasesSelection(phasesSelectionMap);
    pipelineOtherPropertiesForm.setOtherPhaseDescription("The other phase present");

    return pipelineOtherPropertiesForm;
  }

}
