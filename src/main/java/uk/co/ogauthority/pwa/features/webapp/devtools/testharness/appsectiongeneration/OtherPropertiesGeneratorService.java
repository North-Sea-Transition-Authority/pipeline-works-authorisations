package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.OtherPipelineProperty;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PadPipelineOtherPropertiesService;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PipelineOtherPropertiesDataForm;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PropertyPhase;

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

    var form = createOtherPropertiesForm(appFormServiceParams.getApplicationDetail().getResourceType());
    var entities = otherPropertiesService.getPipelineOtherPropertyEntities(appFormServiceParams.getApplicationDetail());
    otherPropertiesService.saveEntitiesUsingForm(form, entities, appFormServiceParams.getApplicationDetail());
  }


  private PipelineOtherPropertiesForm createOtherPropertiesForm(PwaResourceType resourceType) {

    Map<OtherPipelineProperty, PipelineOtherPropertiesDataForm> propertyDataFormMap = new HashMap<>();
    OtherPipelineProperty.asList(resourceType).forEach(property -> {
      var dataForm = new PipelineOtherPropertiesDataForm();
      dataForm.setPropertyAvailabilityOption(PropertyAvailabilityOption.NOT_PRESENT);
      propertyDataFormMap.put(property, dataForm);
    });

    Map<PropertyPhase, String> phasesSelectionMap = new HashMap<>();
    PropertyPhase.asList(resourceType).forEach(phase -> phasesSelectionMap.put(phase, ""));

    var pipelineOtherPropertiesForm = new PipelineOtherPropertiesForm();
    pipelineOtherPropertiesForm.setPropertyDataFormMap(propertyDataFormMap);
    pipelineOtherPropertiesForm.setPhasesSelection(phasesSelectionMap);
    pipelineOtherPropertiesForm.setOtherPhaseDescription("The other phase present");

    return pipelineOtherPropertiesForm;
  }

}
