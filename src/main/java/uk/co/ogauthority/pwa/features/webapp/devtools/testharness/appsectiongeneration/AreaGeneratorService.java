package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadAreaService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PwaFieldForm;

@Service
@Profile("test-harness")
class AreaGeneratorService implements TestHarnessAppFormService {

  private final PadAreaService padAreaService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.FIELD_INFORMATION;

  @Autowired
  public AreaGeneratorService(
      PadAreaService padAreaService) {
    this.padAreaService = padAreaService;
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
    padAreaService.updateFieldInformation(appFormServiceParams.getApplicationDetail(), form);
  }


}
