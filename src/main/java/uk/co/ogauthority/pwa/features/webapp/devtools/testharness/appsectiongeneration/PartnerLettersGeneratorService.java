package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementTestHarnessService;

@Service
@Profile("test-harness")
class PartnerLettersGeneratorService implements TestHarnessAppFormService {

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.PARTNER_LETTERS;
  private final PadFileManagementTestHarnessService padFileManagementTestHarnessService;

  @Autowired
  public PartnerLettersGeneratorService(PadFileManagementTestHarnessService padFileManagementTestHarnessService) {
    this.padFileManagementTestHarnessService = padFileManagementTestHarnessService;
  }

  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);

    padFileManagementTestHarnessService.uploadFileAndMapToForm(
        form,
        appFormServiceParams.getApplicationDetail(),
        FileDocumentType.PARTNER_LETTERS
    );

    form.setPartnerLettersConfirmed(true);
  }

}
