package uk.co.ogauthority.pwa.features.appprocessing.issueconsent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReview;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.ConsentIssueStatus;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.events.ConsentIssueFailedEventPublisher;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriterService;

@ExtendWith(MockitoExtension.class)
class ConsentIssueServiceTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private ConsentReviewService consentReviewService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private ConsentWriterService consentWriterService;

  @Mock
  private IssueConsentEmailsService issueConsentEmailsService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private DocgenService docgenService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private PersonService personService;

  @Mock
  private ConsentIssueFailedEventPublisher consentIssueFailedEventPublisher;

  private ConsentIssueService consentIssueService;

  private PwaApplicationDetail pwaApplicationDetail;
  private Person issuingPerson, caseOfficerPerson;
  private WebUserAccount issuingUser;

  @BeforeEach
  void setUp() {

    pwaApplicationDetail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setResourceType(PwaResourceType.PETROLEUM);
    pwaApplicationDetail.setPwaApplication(app);
    issuingPerson = PersonTestUtil.createDefaultPerson();
    issuingUser = new WebUserAccount(1, issuingPerson);
    caseOfficerPerson = PersonTestUtil.createPersonWithNameFrom(new PersonId(10));

    consentIssueService = new ConsentIssueService(
        pwaApplicationDetailService,
        pwaConsentService,
        consentWriterService,
        issueConsentEmailsService,
        consentReviewService,
        workflowAssignmentService,
        documentInstanceService,
        docgenService,
        camundaWorkflowService,
        personService,
        consentIssueFailedEventPublisher);

  }

  @Test
  void issueConsent() {

    when(personService.getPersonById(caseOfficerPerson.getId())).thenReturn(caseOfficerPerson);

    var docgenRun = new DocgenRun();
    when(docgenService.createDocgenRun(any(), any(), any())).thenReturn(docgenRun);

    var approvedReview = new ConsentReview();
    approvedReview.setCoverLetterText("cover letter");
    approvedReview.setStartedByPersonId(caseOfficerPerson.getId());
    var approvalTime = Instant.now();
    when(consentReviewService.approveConsentReview(pwaApplicationDetail, issuingUser, approvalTime)).thenReturn(approvedReview);

    var consent = new PwaConsent();
    consent.setReference("exampleRef");
    when(pwaConsentService.createConsent(pwaApplicationDetail.getPwaApplication())).thenReturn(consent);

    consentIssueService.issueConsent(pwaApplicationDetail, issuingUser, approvalTime);

    verify(consentReviewService, times(1)).approveConsentReview(pwaApplicationDetail, issuingUser, approvalTime);

    verify(pwaConsentService, times(1)).createConsent(pwaApplicationDetail.getPwaApplication());
    verify(consentWriterService, times(1)).updateConsentedData(pwaApplicationDetail, consent);

    verify(pwaApplicationDetailService, times(1)).updateStatus(pwaApplicationDetail, PwaApplicationStatus.COMPLETE, issuingUser);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(pwaApplicationDetail.getPwaApplication(), ConsentIssueStatus.COMPLETE);
    var workflowTaskInstance = new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(), PwaApplicationWorkflowTask.ISSUING_CONSENT);
    verify(camundaWorkflowService, times(1)).completeTask(workflowTaskInstance);

    verify(issueConsentEmailsService).sendConsentIssuedEmails(
        pwaApplicationDetail,
        consent.getReference(),
        approvedReview.getCoverLetterText(),
        caseOfficerPerson.getEmailAddress(),
        issuingUser.getFullName());

    verify(workflowAssignmentService, times(1)).clearAssignments(pwaApplicationDetail.getPwaApplication());

  }

  @Test
  void failConsentIssue() {

    var exception = mock(Exception.class);
    consentIssueService.failConsentIssue(pwaApplicationDetail, exception, issuingUser);

    verify(pwaApplicationDetailService, times(1))
        .updateStatus(pwaApplicationDetail, PwaApplicationStatus.CONSENT_REVIEW, issuingUser);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(pwaApplicationDetail.getPwaApplication(), ConsentIssueStatus.FAILED);
    var workflowTaskInstance = new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(), PwaApplicationWorkflowTask.ISSUING_CONSENT);
    verify(camundaWorkflowService, times(1)).completeTask(workflowTaskInstance);

    verify(consentIssueFailedEventPublisher, times(1))
        .publishConsentIssueFailedEvent(pwaApplicationDetail, exception, issuingUser);

  }

}
