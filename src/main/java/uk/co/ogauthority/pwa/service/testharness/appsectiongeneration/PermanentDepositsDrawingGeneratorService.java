package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingsService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessPadFileService;

@Service
@Profile("test-harness")
class PermanentDepositsDrawingGeneratorService implements TestHarnessAppFormService {

  private final DepositDrawingsService depositDrawingsService;
  private final PermanentDepositService permanentDepositService;
  private final TestHarnessPadFileService testHarnessPadFileService;

  private static final ApplicationTask LINKED_APP_FORM_TASK = ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS;

  @Autowired
  public PermanentDepositsDrawingGeneratorService(
      DepositDrawingsService depositDrawingsService,
      PermanentDepositService permanentDepositService,
      TestHarnessPadFileService testHarnessPadFileService) {
    this.depositDrawingsService = depositDrawingsService;
    this.permanentDepositService = permanentDepositService;
    this.testHarnessPadFileService = testHarnessPadFileService;
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

    var generatedFileId = testHarnessPadFileService.generateInitialUpload(
        user, detail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS);
    testHarnessPadFileService.setFileIdOnForm(generatedFileId, form.getUploadedFileWithDescriptionForms());
    testHarnessPadFileService.updatePadFiles(
        form, user, detail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, FileUpdateMode.KEEP_UNLINKED_FILES);

    return form;
  }




}
