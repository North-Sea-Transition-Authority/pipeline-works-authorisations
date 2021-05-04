package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@Service
public class ConsentDocumentService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ConsentDocumentEmailService consentDocumentEmailService;
  private final ConsentReviewService consentReviewService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final SendForApprovalCheckerService sendforApprovalCheckerService;

  @Autowired
  public ConsentDocumentService(PwaApplicationDetailService pwaApplicationDetailService,
                                ConsentDocumentEmailService consentDocumentEmailService,
                                ConsentReviewService consentReviewService,
                                CamundaWorkflowService camundaWorkflowService,
                                SendForApprovalCheckerService sendforApprovalCheckerService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.consentDocumentEmailService = consentDocumentEmailService;
    this.consentReviewService = consentReviewService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.sendforApprovalCheckerService = sendforApprovalCheckerService;
  }

  public PreSendForApprovalChecksView getPreSendForApprovalChecksView(PwaApplicationDetail detail) {
    return sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);
  }

  @Transactional
  public void sendForApproval(PwaApplicationDetail pwaApplicationDetail,
                              String coverLetterText,
                              AuthenticatedUserAccount sendingUser) {

    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.CONSENT_REVIEW, sendingUser);

    consentReviewService.startConsentReview(pwaApplicationDetail, coverLetterText, sendingUser.getLinkedPerson());

    consentDocumentEmailService.sendConsentReviewStartedEmail(pwaApplicationDetail, sendingUser.getLinkedPerson());

    var currentTaskWorkflowInstance = new WorkflowTaskInstance(
        pwaApplicationDetail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);
    camundaWorkflowService.completeTask(currentTaskWorkflowInstance);

  }

}
