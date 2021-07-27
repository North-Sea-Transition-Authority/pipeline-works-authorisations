package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.fieldinformation.PadFieldService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormServiceParams;

@Service
@Profile("development")
class FieldGeneratorService implements TestHarnessAppFormService {

  private final PadFieldService padFieldService;

  private final ApplicationTask linkedAppFormTask = ApplicationTask.FIELD_INFORMATION;

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
