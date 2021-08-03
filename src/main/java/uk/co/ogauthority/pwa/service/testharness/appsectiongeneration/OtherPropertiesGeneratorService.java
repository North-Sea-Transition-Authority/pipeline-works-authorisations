package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadPipelineOtherPropertiesService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormServiceParams;

@Service
@Profile("test-harness")
class OtherPropertiesGeneratorService implements TestHarnessAppFormService {

  private final PadPipelineOtherPropertiesService otherPropertiesService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.PIPELINE_OTHER_PROPERTIES;


  @Autowired
  public OtherPropertiesGeneratorService(
      PadPipelineOtherPropertiesService otherPropertiesService) {
    this.otherPropertiesService = otherPropertiesService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = createOtherPropertiesForm();
    var entities = otherPropertiesService.getPipelineOtherPropertyEntities(appFormServiceParams.getApplicationDetail());
    otherPropertiesService.saveEntitiesUsingForm(form, entities, appFormServiceParams.getApplicationDetail());
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
