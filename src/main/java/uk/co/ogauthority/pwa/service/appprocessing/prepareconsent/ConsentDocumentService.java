package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ParallelConsentCheckLog;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.SendConsentForApprovalFormValidator;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent.SendConsentForApprovalForm;
import uk.co.ogauthority.pwa.repository.appprocessing.prepareconsent.ParallelConsentCheckLogRepository;
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
  private final SendConsentForApprovalFormValidator sendConsentForApprovalFormValidator;
  private final ParallelConsentCheckLogRepository parallelConsentCheckLogRepository;
  private final Clock clock;

  @Autowired
  public ConsentDocumentService(PwaApplicationDetailService pwaApplicationDetailService,
                                ConsentDocumentEmailService consentDocumentEmailService,
                                ConsentReviewService consentReviewService,
                                CamundaWorkflowService camundaWorkflowService,
                                SendForApprovalCheckerService sendforApprovalCheckerService,
                                SendConsentForApprovalFormValidator sendConsentForApprovalFormValidator,
                                ParallelConsentCheckLogRepository parallelConsentCheckLogRepository,
                                @Qualifier("utcClock") Clock clock) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.consentDocumentEmailService = consentDocumentEmailService;
    this.consentReviewService = consentReviewService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.sendforApprovalCheckerService = sendforApprovalCheckerService;
    this.sendConsentForApprovalFormValidator = sendConsentForApprovalFormValidator;
    this.parallelConsentCheckLogRepository = parallelConsentCheckLogRepository;
    this.clock = clock;
  }

  public PreSendForApprovalChecksView getPreSendForApprovalChecksView(PwaApplicationDetail detail) {
    return sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);
  }

  @Transactional
  public void sendForApproval(PwaApplicationDetail pwaApplicationDetail,
                              String coverLetterText,
                              AuthenticatedUserAccount sendingUser,
                              List<ParallelConsentView> parallelConsentViewList) {

    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.CONSENT_REVIEW, sendingUser);

    var consentReview = consentReviewService.startConsentReview(pwaApplicationDetail, coverLetterText, sendingUser.getLinkedPerson());

    consentDocumentEmailService.sendConsentReviewStartedEmail(pwaApplicationDetail, sendingUser.getLinkedPerson());

    var instant = clock.instant();

    var parallelConsentCheckLogs = parallelConsentViewList.stream()
        .map(parallelConsentView -> new ParallelConsentCheckLog(
            consentReview,
            parallelConsentView.getPwaConsentId(),
            sendingUser.getLinkedPerson(),
            instant
        ))
        .collect(Collectors.toList());

    if (!parallelConsentCheckLogs.isEmpty()) {
      parallelConsentCheckLogRepository.saveAll(parallelConsentCheckLogs);
    }

    var currentTaskWorkflowInstance = new WorkflowTaskInstance(
        pwaApplicationDetail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);
    camundaWorkflowService.completeTask(currentTaskWorkflowInstance);

  }

  public void validateSendConsentFormUsingPreApprovalChecks(SendConsentForApprovalForm form,
                                                            BindingResult formBindingResult,
                                                            PreSendForApprovalChecksView preSendForApprovalChecksView) {

    sendConsentForApprovalFormValidator.validate(form, formBindingResult, preSendForApprovalChecksView);
  }

}
