package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.OptionsTemplateForm;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementTestHarnessService;

@Service
@Profile("test-harness")
class OptionsTemplateGeneratorService implements TestHarnessAppFormService {

  private static final ApplicationTask LINKED_APP_FORM_TASK = ApplicationTask.OPTIONS_TEMPLATE;
  private final PadFileManagementTestHarnessService padFileManagementTestHarnessService;

  @Autowired
  public OptionsTemplateGeneratorService(PadFileManagementTestHarnessService padFileManagementTestHarnessService) {
    this.padFileManagementTestHarnessService = padFileManagementTestHarnessService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return LINKED_APP_FORM_TASK;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {
    var detail = appFormServiceParams.getApplicationDetail();

    var form = new OptionsTemplateForm();

    padFileManagementTestHarnessService.uploadFileAndMapToForm(
        form,
        detail,
        FileDocumentType.OPTIONS_TEMPLATE
    );
  }


}
