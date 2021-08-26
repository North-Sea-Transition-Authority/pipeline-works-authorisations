package uk.co.ogauthority.pwa.service.appprocessing.consentissue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
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

@RunWith(MockitoJUnitRunner.class)
public class ConsentIssueServiceTest {

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

  private ConsentIssueService consentIssueService;

  private PwaApplicationDetail pwaApplicationDetail;
  private Person issuingPerson;
  private WebUserAccount issuingUser;

  @Before
  public void setUp() {

    pwaApplicationDetail = new PwaApplicationDetail();
    var app = new PwaApplication();
    pwaApplicationDetail.setPwaApplication(app);
    issuingPerson = PersonTestUtil.createDefaultPerson();
    issuingUser = new WebUserAccount(1, issuingPerson);

    consentIssueService = new ConsentIssueService(
        pwaApplicationDetailService,
        pwaConsentService,
        consentWriterService,
        issueConsentEmailsService,
        consentReviewService,
        workflowAssignmentService,
        documentInstanceService,
        docgenService,
        camundaWorkflowService);

  }

  @Test
  public void issueConsent() {

    var docgenRun = new DocgenRun();
    when(docgenService.createDocgenRun(any(), any(), any())).thenReturn(docgenRun);

    var approvedReview = new ConsentReview();
    approvedReview.setCoverLetterText("cover letter");
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

    verify(issueConsentEmailsService).sendConsentIssuedEmails(pwaApplicationDetail, approvedReview.getCoverLetterText(), issuingUser.getFullName());

    verify(workflowAssignmentService, times(1)).clearAssignments(pwaApplicationDetail.getPwaApplication());

  }

}