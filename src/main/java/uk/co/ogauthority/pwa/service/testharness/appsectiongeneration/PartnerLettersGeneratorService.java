package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PadPartnerLettersService;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessPadFileService;

@Service
@Profile("test-harness")
class PartnerLettersGeneratorService implements TestHarnessAppFormService {

  private final PadPartnerLettersService padPartnerLettersService;
  private final TestHarnessPadFileService testHarnessPadFileService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.PARTNER_LETTERS;

  @Autowired
  public PartnerLettersGeneratorService(
      PadPartnerLettersService padPartnerLettersService,
      TestHarnessPadFileService testHarnessPadFileService) {
    this.padPartnerLettersService = padPartnerLettersService;
    this.testHarnessPadFileService = testHarnessPadFileService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var uploadedFileId = testHarnessPadFileService.generateInitialUpload(
        appFormServiceParams.getUser(), appFormServiceParams.getApplicationDetail(), ApplicationDetailFilePurpose.PARTNER_LETTERS);
    var form = createForm(uploadedFileId);
    padPartnerLettersService.saveEntityUsingForm(appFormServiceParams.getApplicationDetail(), form, appFormServiceParams.getUser());
  }


  private PartnerLettersForm createForm(String uploadedFileId) {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);

    testHarnessPadFileService.setFileIdOnForm(uploadedFileId, form.getUploadedFileWithDescriptionForms());

    return form;
  }




}
