package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.OptionsTemplateForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormServiceParams;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessFileService;

@Service
@Profile("test-harness")
class OptionsTemplateGeneratorService implements TestHarnessAppFormService {

  private final TestHarnessFileService testHarnessFileService;

  private static final ApplicationTask LINKED_APP_FORM_TASK = ApplicationTask.OPTIONS_TEMPLATE;

  @Autowired
  public OptionsTemplateGeneratorService(
      TestHarnessFileService testHarnessFileService) {
    this.testHarnessFileService = testHarnessFileService;
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

    var generatedFileId = testHarnessFileService.generateInitialUpload(
        user, detail, ApplicationDetailFilePurpose.OPTIONS_TEMPLATE);
    testHarnessFileService.setFileIdOnForm(generatedFileId, form.getUploadedFileWithDescriptionForms());
    testHarnessFileService.updatePadFiles(
        form, user, detail, ApplicationDetailFilePurpose.OPTIONS_TEMPLATE, FileUpdateMode.DELETE_UNLINKED_FILES);
  }


}
