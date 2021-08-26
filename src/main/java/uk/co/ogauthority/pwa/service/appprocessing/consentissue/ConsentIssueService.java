package uk.co.ogauthority.pwa.service.appprocessing.consentissue;

import java.time.Instant;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.IssueConsentEmailsService;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.ConsentIssueStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriterService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@Service
public class ConsentIssueService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaConsentService pwaConsentService;
  private final ConsentWriterService consentWriterService;
  private final IssueConsentEmailsService issueConsentEmailsService;
  private final ConsentReviewService consentReviewService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final DocumentInstanceService documentInstanceService;
  private final DocgenService docgenService;
  private final CamundaWorkflowService camundaWorkflowService;

  private static final DocGenType DOC_GEN_TYPE = DocGenType.FULL;

  @Autowired
  public ConsentIssueService(PwaApplicationDetailService pwaApplicationDetailService,
                             PwaConsentService pwaConsentService,
                             ConsentWriterService consentWriterService,
                             IssueConsentEmailsService issueConsentEmailsService,
                             ConsentReviewService consentReviewService,
                             WorkflowAssignmentService workflowAssignmentService,
                             DocumentInstanceService documentInstanceService,
                             DocgenService docgenService,
                             CamundaWorkflowService camundaWorkflowService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaConsentService = pwaConsentService;
    this.consentWriterService = consentWriterService;
    this.issueConsentEmailsService = issueConsentEmailsService;
    this.consentReviewService = consentReviewService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.documentInstanceService = documentInstanceService;
    this.docgenService = docgenService;
    this.camundaWorkflowService = camundaWorkflowService;
  }

  @Transactional
  public void issueConsent(PwaApplicationDetail pwaApplicationDetail,
                           WebUserAccount issuingUser,
                           Instant approvalTime) {

    var approvedReview = consentReviewService.approveConsentReview(pwaApplicationDetail, issuingUser, approvalTime);

    var consent = pwaConsentService.createConsent(pwaApplicationDetail.getPwaApplication());
    consentWriterService.updateConsentedData(pwaApplicationDetail, consent);

    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.COMPLETE, issuingUser);

    var docInstance = documentInstanceService
        .getDocumentInstanceOrError(pwaApplicationDetail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    var docgenRun = docgenService.createDocgenRun(docInstance, DOC_GEN_TYPE, issuingUser.getLinkedPerson());
    pwaConsentService.setDocgenRunId(consent, docgenRun);
    docgenService.processDocgenRun(docgenRun);

    camundaWorkflowService.setWorkflowProperty(pwaApplicationDetail.getPwaApplication(), ConsentIssueStatus.COMPLETE);
    var workflowTaskInstance = new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.ISSUING_CONSENT);
    camundaWorkflowService.completeTask(workflowTaskInstance);

    issueConsentEmailsService.sendConsentIssuedEmails(
        pwaApplicationDetail, approvedReview.getCoverLetterText(), issuingUser.getFullName());

    // have to clear assignments last so that we can email the assigned CO in the previous step
    workflowAssignmentService.clearAssignments(pwaApplicationDetail.getPwaApplication());

  }

}
