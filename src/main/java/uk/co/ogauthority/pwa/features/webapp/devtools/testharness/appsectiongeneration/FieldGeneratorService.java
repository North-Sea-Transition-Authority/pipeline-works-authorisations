package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadFieldService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PwaFieldForm;

@Service
@Profile("test-harness")
class FieldGeneratorService implements TestHarnessAppFormService {

  private final PadFieldService padFieldService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.FIELD_INFORMATION;

  @Autowired
  public FieldGeneratorService(
      PadFieldService padFieldService) {
    this.padFieldService = padFieldService;
  }

  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = new PwaFieldForm();
    form.setFieldIds(List.of("2692"));
    form.setLinkedToField(true);
    padFieldService.updateFieldInformation(appFormServiceParams.getApplicationDetail(), form);
  }


}
