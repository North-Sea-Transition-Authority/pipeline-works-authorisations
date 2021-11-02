package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.OptionsTemplateForm;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessPadFileService;

@Service
@Profile("test-harness")
class OptionsTemplateGeneratorService implements TestHarnessAppFormService {

  private final TestHarnessPadFileService testHarnessPadFileService;

  private static final ApplicationTask LINKED_APP_FORM_TASK = ApplicationTask.OPTIONS_TEMPLATE;

  @Autowired
  public OptionsTemplateGeneratorService(
      TestHarnessPadFileService testHarnessPadFileService) {
    this.testHarnessPadFileService = testHarnessPadFileService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return LINKED_APP_FORM_TASK;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {
    var detail = appFormServiceParams.getApplicationDetail();
    var user = appFormServiceParams.getUser();

    var form = new OptionsTemplateForm();

    var generatedFileId = testHarnessPadFileService.generateInitialUpload(
        user, detail, ApplicationDetailFilePurpose.OPTIONS_TEMPLATE);
    testHarnessPadFileService.setFileIdOnForm(generatedFileId, form.getUploadedFileWithDescriptionForms());
    testHarnessPadFileService.updatePadFiles(
        form, user, detail, ApplicationDetailFilePurpose.OPTIONS_TEMPLATE, FileUpdateMode.DELETE_UNLINKED_FILES);
  }


}
