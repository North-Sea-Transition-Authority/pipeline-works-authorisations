package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsentDocumentServiceTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private ConsentDocumentEmailService consentDocumentEmailService;

  @Mock
  private ConsentReviewService consentReviewService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private SendForApprovalCheckerService sendforApprovalCheckerService;

  private ConsentDocumentService consentDocumentService;

  private PwaApplicationDetail detail;

  private final Person person = PersonTestUtil.createDefaultPerson();
  private final AuthenticatedUserAccount authUser = new AuthenticatedUserAccount(new WebUserAccount(1, person), Set.of());

  @Before
  public void setUp() throws Exception {

    consentDocumentService = new ConsentDocumentService(
        pwaApplicationDetailService,
        consentDocumentEmailService,
        consentReviewService,
        camundaWorkflowService,
        sendforApprovalCheckerService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  public void sendForApproval_verifyServiceCalls() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    consentDocumentService.sendForApproval(detail, "cover letter my text", authUser);

    verify(pwaApplicationDetailService, times(1)).updateStatus(detail, PwaApplicationStatus.CONSENT_REVIEW, authUser);

    verify(consentReviewService, times(1)).startConsentReview(detail, "cover letter my text", person);

    verify(consentDocumentEmailService, times(1)).sendConsentReviewStartedEmail(detail, person);

    var workflowTaskInstance = new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);
    verify(camundaWorkflowService, times(1)).completeTask(workflowTaskInstance);

  }

}