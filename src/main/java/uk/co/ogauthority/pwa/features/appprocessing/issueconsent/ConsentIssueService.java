package uk.co.ogauthority.pwa.features.appprocessing.issueconsent;

import java.time.Instant;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.ConsentIssueStatus;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.events.ConsentIssueFailedEventPublisher;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriterService;

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
  private final PersonService personService;
  private final ConsentIssueFailedEventPublisher consentIssueFailedEventPublisher;

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
                             CamundaWorkflowService camundaWorkflowService,
                             PersonService personService,
                             ConsentIssueFailedEventPublisher consentIssueFailedEventPublisher) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaConsentService = pwaConsentService;
    this.consentWriterService = consentWriterService;
    this.issueConsentEmailsService = issueConsentEmailsService;
    this.consentReviewService = consentReviewService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.documentInstanceService = documentInstanceService;
    this.docgenService = docgenService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.personService = personService;
    this.consentIssueFailedEventPublisher = consentIssueFailedEventPublisher;
  }

  @Transactional
  public void issueConsent(PwaApplicationDetail pwaApplicationDetail,
                           WebUserAccount issuingUser,
                           Instant approvalTime) {

    var approvedReview = consentReviewService.approveConsentReview(pwaApplicationDetail, issuingUser, approvalTime);

    var consent = pwaConsentService.createConsent(pwaApplicationDetail.getPwaApplication());
    consentWriterService.updateConsentedData(pwaApplicationDetail, consent);

    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.COMPLETE, issuingUser);

    var docMnem = DocumentTemplateMnem.getMnemFromResourceType(pwaApplicationDetail.getResourceType());
    var docInstance = documentInstanceService
        .getDocumentInstanceOrError(pwaApplicationDetail.getPwaApplication(), docMnem);

    var docgenRun = docgenService.createDocgenRun(docInstance, DOC_GEN_TYPE, issuingUser.getLinkedPerson());
    pwaConsentService.setDocgenRunId(consent, docgenRun);
    docgenService.processDocgenRun(docgenRun);

    completeConsentIssueTask(pwaApplicationDetail, ConsentIssueStatus.COMPLETE);

    var caseOfficerPerson = personService.getPersonById(approvedReview.getStartedByPersonId());

    issueConsentEmailsService.sendConsentIssuedEmails(
        pwaApplicationDetail,
        consent.getReference(),
        approvedReview.getCoverLetterText(),
        caseOfficerPerson.getEmailAddress(),
        issuingUser.getFullName());

    // have to clear assignments last so that we can email the assigned CO in the previous step
    workflowAssignmentService.clearAssignments(pwaApplicationDetail.getPwaApplication());

  }

  @Transactional
  public void failConsentIssue(PwaApplicationDetail pwaApplicationDetail,
                               Exception exception,
                               WebUserAccount issuingUser) {

    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.CONSENT_REVIEW, issuingUser);

    completeConsentIssueTask(pwaApplicationDetail, ConsentIssueStatus.FAILED);

    consentIssueFailedEventPublisher.publishConsentIssueFailedEvent(pwaApplicationDetail, exception, issuingUser);

  }

  private void completeConsentIssueTask(PwaApplicationDetail pwaApplicationDetail, ConsentIssueStatus issueStatus) {

    camundaWorkflowService.setWorkflowProperty(pwaApplicationDetail.getPwaApplication(), issueStatus);
    var workflowTaskInstance = new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.ISSUING_CONSENT);
    camundaWorkflowService.completeTask(workflowTaskInstance);

  }

}
