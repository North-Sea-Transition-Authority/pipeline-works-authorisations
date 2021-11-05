package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.filehelper.TestHarnessAppFileService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.FinalisePublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeApprovalService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDocumentUpdateService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDraftService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeApprovalResult;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;

@Service
@Profile("test-harness")
class PublicNoticeGeneratorService implements TestHarnessAppProcessingService {

  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeDraftService publicNoticeDraftService;
  private final PublicNoticeApprovalService publicNoticeApprovalService;
  private final PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService;
  private final FinalisePublicNoticeService finalisePublicNoticeService;
  private final TestHarnessAppFileService testHarnessAppFileService;

  private static final PwaAppProcessingTask LINKED_APP_PROCESSING_TASK = PwaAppProcessingTask.PUBLIC_NOTICE;

  @Autowired
  public PublicNoticeGeneratorService(
      PublicNoticeService publicNoticeService,
      PublicNoticeDraftService publicNoticeDraftService,
      PublicNoticeApprovalService publicNoticeApprovalService,
      PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService,
      FinalisePublicNoticeService finalisePublicNoticeService,
      TestHarnessAppFileService testHarnessAppFileService) {
    this.publicNoticeService = publicNoticeService;
    this.publicNoticeDraftService = publicNoticeDraftService;
    this.publicNoticeApprovalService = publicNoticeApprovalService;
    this.publicNoticeDocumentUpdateService = publicNoticeDocumentUpdateService;
    this.finalisePublicNoticeService = finalisePublicNoticeService;
    this.testHarnessAppFileService = testHarnessAppFileService;
  }


  @Override
  public PwaAppProcessingTask getLinkedAppProcessingTask() {
    return LINKED_APP_PROCESSING_TASK;
  }



  @Override
  public void generateAppProcessingTaskData(TestHarnessAppProcessingProperties appProcessingProps) {

    createPublicNoticeDraft(appProcessingProps);
    approvePublicNotice(appProcessingProps);
    updatePublicNoticeDocument(appProcessingProps);
    finalisePublicNotice(appProcessingProps);
    endPublicNotice(appProcessingProps);
  }


  private void generatePublicNoticeDocument(TestHarnessAppProcessingProperties appProcessingProps,
                                            UploadMultipleFilesWithDescriptionForm form) {

    var generatedFileId = testHarnessAppFileService.generateInitialUpload(
        appProcessingProps.getCaseOfficerAua(), appProcessingProps.getPwaApplication(), AppFilePurpose.PUBLIC_NOTICE);

    testHarnessAppFileService.setFileIdOnForm(generatedFileId, form.getUploadedFileWithDescriptionForms());
    testHarnessAppFileService.updateAppFiles(form, appProcessingProps.getCaseOfficerAua(), appProcessingProps.getPwaApplication(),
        AppFilePurpose.PUBLIC_NOTICE, FileUpdateMode.KEEP_UNLINKED_FILES);
  }

  private void createPublicNoticeDraft(TestHarnessAppProcessingProperties appProcessingProps) {

    var form = new PublicNoticeDraftForm();
    form.setCoverLetterText("My public notice cover letter text");
    form.setReason(PublicNoticeRequestReason.ALL_CONSULTEES_CONTENT);
    generatePublicNoticeDocument(appProcessingProps, form);

    publicNoticeDraftService.submitPublicNoticeDraft(form, appProcessingProps.getPwaApplication(), appProcessingProps.getCaseOfficerAua());
  }

  private void approvePublicNotice(TestHarnessAppProcessingProperties appProcessingProps) {

    var form = new PublicNoticeApprovalForm();
    form.setRequestApproved(PwaApplicationPublicNoticeApprovalResult.REQUEST_APPROVED);
    publicNoticeApprovalService.updatePublicNoticeRequest(
        form, appProcessingProps.getPwaApplication(), appProcessingProps.getPwaManagerAua());

  }

  private void updatePublicNoticeDocument(TestHarnessAppProcessingProperties appProcessingProps) {

    var form = new UpdatePublicNoticeDocumentForm();
    generatePublicNoticeDocument(appProcessingProps, form);

    publicNoticeDocumentUpdateService.updatePublicNoticeDocumentAndTransitionWorkflow(
        appProcessingProps.getPwaApplication(), form, appProcessingProps.getApplicantAua());
  }


  private void finalisePublicNotice(TestHarnessAppProcessingProperties appProcessingProps) {

    var form = new FinalisePublicNoticeForm();
    var today = LocalDate.now();
    form.setStartDay(today.getDayOfMonth());
    form.setStartMonth(today.getMonthValue());
    form.setStartYear(today.getYear());
    form.setDaysToBePublishedFor(0);

    finalisePublicNoticeService.finalisePublicNotice(
        appProcessingProps.getPwaApplication(), form, appProcessingProps.getCaseOfficerAua());
  }


  private void endPublicNotice(TestHarnessAppProcessingProperties appProcessingProps) {
    var publicNotice = publicNoticeService.getLatestPublicNotice(appProcessingProps.getPwaApplication());
    publicNoticeService.endPublicNotices(List.of(publicNotice));
  }




}
