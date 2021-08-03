package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormServiceParams;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessFileService;

@Service
@Profile("test-harness")
class PermanentDepositsDrawingGeneratorService implements TestHarnessAppFormService {

  private final DepositDrawingsService depositDrawingsService;
  private final PermanentDepositService permanentDepositService;
  private final TestHarnessFileService testHarnessFileService;

  private static final ApplicationTask LINKED_APP_FORM_TASK = ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS;

  @Autowired
  public PermanentDepositsDrawingGeneratorService(
      DepositDrawingsService depositDrawingsService,
      PermanentDepositService permanentDepositService,
      TestHarnessFileService testHarnessFileService) {
    this.depositDrawingsService = depositDrawingsService;
    this.permanentDepositService = permanentDepositService;
    this.testHarnessFileService = testHarnessFileService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return LINKED_APP_FORM_TASK;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {
    depositDrawingsService.addDrawing(
        appFormServiceParams.getApplicationDetail(), createForm(appFormServiceParams), appFormServiceParams.getUser());
  }


  private PermanentDepositDrawingForm createForm(TestHarnessAppFormServiceParams appFormServiceParams) {

    var detail = appFormServiceParams.getApplicationDetail();
    var user = appFormServiceParams.getUser();

    var form = new PermanentDepositDrawingForm();

    var selectedDepositIds = permanentDepositService.getPermanentDeposits(detail)
        .stream().map(deposit -> String.valueOf(deposit.getId()))
        .collect(Collectors.toSet());

    form.setSelectedDeposits(selectedDepositIds);
    form.setReference("Test_Harness_Deposit_Drawing_Reference");

    var generatedFileId = testHarnessFileService.generateInitialUpload(
        user, detail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS);
    testHarnessFileService.setFileIdOnForm(generatedFileId, form.getUploadedFileWithDescriptionForms());
    testHarnessFileService.updatePadFiles(
        form, user, detail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, FileUpdateMode.KEEP_UNLINKED_FILES);

    return form;
  }




}
