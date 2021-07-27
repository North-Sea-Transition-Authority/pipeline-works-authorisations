package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.partnerletters.PadPartnerLettersService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormServiceParams;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessFileService;

@Service
@Profile("development")
class PartnerLettersGeneratorService implements TestHarnessAppFormService {

  private final PadPartnerLettersService padPartnerLettersService;
  private final TestHarnessFileService testHarnessFileService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.PARTNER_LETTERS;

  @Autowired
  public PartnerLettersGeneratorService(
      PadPartnerLettersService padPartnerLettersService,
      TestHarnessFileService testHarnessFileService) {
    this.padPartnerLettersService = padPartnerLettersService;
    this.testHarnessFileService = testHarnessFileService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var uploadedFileId = testHarnessFileService.generateInitialUpload(
        appFormServiceParams.getUser(), appFormServiceParams.getApplicationDetail(), ApplicationDetailFilePurpose.PARTNER_LETTERS);
    var form = createForm(uploadedFileId);
    padPartnerLettersService.saveEntityUsingForm(appFormServiceParams.getApplicationDetail(), form, appFormServiceParams.getUser());
  }


  private PartnerLettersForm createForm(String uploadedFileId) {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);

    testHarnessFileService.setFileIdOnForm(uploadedFileId, form.getUploadedFileWithDescriptionForms());

    return form;
  }




}
