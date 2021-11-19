package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.ConsentDocumentService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval.SendConsentForApprovalForm;
import uk.co.ogauthority.pwa.service.documents.DocumentService;

@Service
@Profile("test-harness")
class SendForApprovalGeneratorService implements TestHarnessAppProcessingService {

  private final ConsentDocumentService consentDocumentService;
  private final DocumentService documentService;

  private static final PwaAppProcessingTask LINKED_APP_PROCESSING_TASK = PwaAppProcessingTask.PREPARE_CONSENT;

  @Autowired
  public SendForApprovalGeneratorService(
      ConsentDocumentService consentDocumentService,
      DocumentService documentService) {
    this.consentDocumentService = consentDocumentService;
    this.documentService = documentService;
  }


  @Override
  public PwaAppProcessingTask getLinkedAppProcessingTask() {
    return LINKED_APP_PROCESSING_TASK;
  }


  @Override
  public void generateAppProcessingTaskData(TestHarnessAppProcessingProperties appProcessingProps) {
    loadConsentDocument(appProcessingProps);
    sendForApproval(appProcessingProps);
  }


  private void loadConsentDocument(TestHarnessAppProcessingProperties appProcessingProps) {
    documentService.createDocumentInstance(
        appProcessingProps.getPwaApplication(),
        appProcessingProps.getCaseOfficerAua().getLinkedPerson());
  }

  private void sendForApproval(TestHarnessAppProcessingProperties appProcessingProps) {
    var form = new SendConsentForApprovalForm();
    form.setCoverLetterText("My consent email cover letter text");
    form.setParallelConsentsReviewedIfApplicable(false);

    consentDocumentService.sendForApproval(
        appProcessingProps.getPwaApplicationDetail(),
        "My consent email cover letter text",
        appProcessingProps.getCaseOfficerAua(),
        List.of());
  }


}
