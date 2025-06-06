package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfoService;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PipelineTechInfoForm;

@Service
@Profile("test-harness")
class GeneralTechDetailsGeneratorService implements TestHarnessAppFormService {

  private final PadPipelineTechInfoService techInfoService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.GENERAL_TECH_DETAILS;


  @Autowired
  public GeneralTechDetailsGeneratorService(
      PadPipelineTechInfoService techInfoService) {
    this.techInfoService = techInfoService;
  }

  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = createForm();
    var entity = techInfoService.getPipelineTechInfoEntity(appFormServiceParams.getApplicationDetail());
    techInfoService.saveEntityUsingForm(form, entity);
  }


  private PipelineTechInfoForm createForm() {

    var form = new PipelineTechInfoForm();
    form.setEstimatedAssetLife(50);
    form.setPipelineDesignedToStandards(false);
    form.setCorrosionDescription("My description of the corrosion management strategy");
    form.setPlannedPipelineTieInPoints(false);
    return form;
  }

}
