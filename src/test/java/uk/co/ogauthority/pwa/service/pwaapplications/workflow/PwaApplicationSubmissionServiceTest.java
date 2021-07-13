package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDataCleanupService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationSubmissionServiceTest {


  private static final String SUBMISSION_DESC = "desc;";
  private static final PwaApplicationWorkflowTask DEFAULT_COMPLETE_WORKFLOW_TASK = PwaApplicationWorkflowTask.PREPARE_APPLICATION;
  private static final PwaApplicationSubmitResult SUBMISSION_RESULT = PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION;
  private static final PwaApplicationStatus DEFAULT_STATUS = PwaApplicationStatus.CASE_OFFICER_REVIEW;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private PwaApplicationDataCleanupService dataCleanupService;

  @Mock
  private ApplicationSubmissionServiceProvider applicationSubmissionServiceProvider;

  @Mock
  private ApplicationSubmissionService applicationSubmissionService;

  private PwaApplicationSubmissionService pwaApplicationSubmissionService;

  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount user = new WebUserAccount(1);

  private InOrder verifyOrder;

  @Before
  public void setup() {
    pwaApplicationSubmissionService = new PwaApplicationSubmissionService(
        pwaApplicationDetailService,
        camundaWorkflowService,
        dataCleanupService,
        applicationSubmissionServiceProvider
    );

    verifyOrder = Mockito.inOrder(
        pwaApplicationDetailService,
        camundaWorkflowService,
        dataCleanupService,
        applicationSubmissionServiceProvider,
        // also test calls on provided submission service
        applicationSubmissionService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    when(applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail))
        .thenReturn(applicationSubmissionService);
    when(applicationSubmissionService.getTaskToComplete()).thenReturn(DEFAULT_COMPLETE_WORKFLOW_TASK);
    when(applicationSubmissionService.getSubmittedApplicationDetailStatus(pwaApplicationDetail)).thenReturn(DEFAULT_STATUS);

  }


  @Test(expected = IllegalArgumentException.class)
  public void submitApplication_whenDetailIsNotTip() {
    pwaApplicationDetail.setTipFlag(false);
    pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail, SUBMISSION_DESC);
  }


  @Test
  public void submitApplication_throwsErrorWhenNotIndustryEditable() {
    var invalidSubmitStatuses = EnumSet.allOf(PwaApplicationStatus.class);
    invalidSubmitStatuses.removeAll(ApplicationState.INDUSTRY_EDITABLE.getStatuses());

    // test each status where error expected
    for (PwaApplicationStatus invalidStatus : invalidSubmitStatuses) {

      PwaApplicationTestUtil.tryAssertionWithStatus(
          invalidStatus,
          (status) -> {
            pwaApplicationDetail.setStatus(status);
            assertThatThrownBy(() ->
                pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail, SUBMISSION_DESC)).isInstanceOf(
                IllegalArgumentException.class);
          }
      );
    }
  }


  @Test
  public void submitApplication_whenDetailIsTipDraft_serviceInteractions_whenNoSubmitResultProvided() {

    pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail, SUBMISSION_DESC);

    verifyOrder.verify(applicationSubmissionServiceProvider).getSubmissionService(pwaApplicationDetail);
    verifyOrder.verify(applicationSubmissionService).doBeforeSubmit(pwaApplicationDetail, user.getLinkedPerson(), SUBMISSION_DESC);
    verifyOrder.verify(dataCleanupService).cleanupData(pwaApplicationDetail, user);

    verifyOrder.verify(camundaWorkflowService).completeTask(
        new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(), DEFAULT_COMPLETE_WORKFLOW_TASK)
    );

    verifyOrder.verify(pwaApplicationDetailService).setSubmitted(pwaApplicationDetail, user, DEFAULT_STATUS);
    verifyOrder.verify(applicationSubmissionService).doAfterSubmit(pwaApplicationDetail);

    verifyOrder.verifyNoMoreInteractions();
  }


  @Test
  public void submitApplication_whenDetailIsTipDraft_serviceInteractions_whenSubmitResultProvided() {

    when(applicationSubmissionService.getSubmissionWorkflowResult())
        .thenReturn(Optional.of(SUBMISSION_RESULT));

    pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail, SUBMISSION_DESC);

    verifyOrder.verify(applicationSubmissionServiceProvider).getSubmissionService(pwaApplicationDetail);
    verifyOrder.verify(applicationSubmissionService).doBeforeSubmit(pwaApplicationDetail, user.getLinkedPerson(), SUBMISSION_DESC);
    verifyOrder.verify(dataCleanupService).cleanupData(pwaApplicationDetail, user);

    verifyOrder.verify(camundaWorkflowService).setWorkflowProperty(pwaApplicationDetail.getPwaApplication(), SUBMISSION_RESULT);
    verifyOrder.verify(camundaWorkflowService).completeTask(
        new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(), DEFAULT_COMPLETE_WORKFLOW_TASK)
    );

    verifyOrder.verify(pwaApplicationDetailService).setSubmitted(pwaApplicationDetail, user, DEFAULT_STATUS);
    verifyOrder.verify(applicationSubmissionService).doAfterSubmit(pwaApplicationDetail);

    verifyOrder.verifyNoMoreInteractions();
  }


}