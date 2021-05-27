package uk.co.ogauthority.pwa.service.appprocessing.initialreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestSpecification;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.ApplicationFeeReport;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.ApplicationFeeReportTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.ApplicationFeeService;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class InitialReviewServiceTest {

  private static String WAIVE_REASON = "REASON";

  private static String FEE_REPORT_SUMMARY = "SUMMARY";
  private static int FEE_REPORT_AMOUNT = 200;
  private static String FEE_ITEM_DESC = "FEE_ITEM";
  private static int FEE_ITEM_AMOUNT = 100;

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Mock
  private ApplicationChargeRequestService applicationChargeRequestService;

  @Mock
  private AssignCaseOfficerService assignCaseOfficerService;

  @Mock
  private ApplicationFeeService applicationFeeService;

  @Captor
  private ArgumentCaptor<ApplicationChargeRequestSpecification> chargeRequestSpecCaptor;

  private InitialReviewService initialReviewService;

  private PwaApplicationDetail detail;

  private PwaApplication app;

  private Person pwaManagerPerson;
  private AuthenticatedUserAccount pwaManagerUser;

  private ApplicationFeeReport applicationFeeReport;

  private Person caseOfficerPerson;

  @Before
  public void setUp() {

    pwaManagerPerson = new Person(1, "Oga", "Person", "manager@pwa.co.uk", null);
    pwaManagerUser = new AuthenticatedUserAccount(new WebUserAccount(1, pwaManagerPerson), EnumSet.allOf(PwaUserPrivilege.class));

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);
    app = detail.getPwaApplication();
    app.setAppReference("PA/2/1");
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    detail.setSubmittedByPersonId(pwaManagerPerson.getId());

    applicationFeeReport = ApplicationFeeReportTestUtil.createReport(
        app,
        FEE_REPORT_AMOUNT,
        FEE_REPORT_SUMMARY,
        List.of(ApplicationFeeReportTestUtil.createApplicationFeeItem(FEE_ITEM_DESC, FEE_ITEM_AMOUNT))
    );
    when(applicationFeeService.getApplicationFeeReport(detail)).thenReturn(applicationFeeReport);

    caseOfficerPerson = new Person(555, "Test", "CO", "case-officer@pwa.co.uk", null);

    initialReviewService = new InitialReviewService(
        detailService,
        camundaWorkflowService,
        applicationUpdateRequestService,
        applicationChargeRequestService,
        applicationFeeService,
        assignCaseOfficerService
    );

  }

  @Test
  public void acceptApplication_success_paymentWaived() {

    initialReviewService.acceptApplication(
        detail,
        caseOfficerPerson.getId(),
        InitialReviewPaymentDecision.PAYMENT_WAIVED,
        WAIVE_REASON,
        pwaManagerUser);

    verify(detailService, times(1)).setInitialReviewApproved(detail, pwaManagerUser, InitialReviewPaymentDecision.PAYMENT_WAIVED);
    verify(camundaWorkflowService, times(1))
        .completeTask(new WorkflowTaskInstance(app, PwaApplicationWorkflowTask.APPLICATION_REVIEW));
    verify(camundaWorkflowService, times(1))
        .setWorkflowProperty(
            detail.getPwaApplication(),
            InitialReviewPaymentDecision.PAYMENT_WAIVED.getPwaApplicationInitialReviewResult()
        );
    verify(applicationChargeRequestService, times(1)).createPwaAppChargeRequest(eq(pwaManagerPerson),chargeRequestSpecCaptor.capture() );

    verify(assignCaseOfficerService, times(1))
        .assignCaseOfficer(caseOfficerPerson.getId(), detail, pwaManagerUser);

    assertThat(chargeRequestSpecCaptor.getValue()).satisfies(requestSpecification -> {

      assertThat(requestSpecification.getPwaApplication()).isEqualTo(app);
      assertThat(requestSpecification.getPwaAppChargeRequestStatus()).isEqualTo(PwaAppChargeRequestStatus.WAIVED);
      assertThat(requestSpecification.getChargeSummary()).isEqualTo(FEE_REPORT_SUMMARY);
      assertThat(requestSpecification.getTotalPennies()).isEqualTo(FEE_REPORT_AMOUNT);
      assertThat(requestSpecification.getChargeWaivedReason()).isEqualTo(WAIVE_REASON);
      assertThat(requestSpecification.getOnPaymentCompleteCaseOfficerPersonId()).isEqualTo(caseOfficerPerson.getId());
      assertThat(requestSpecification.getApplicationChargeItems()).hasOnlyOneElementSatisfying(applicationChargeItem -> {
            assertThat(applicationChargeItem.getDescription()).isEqualTo(FEE_ITEM_DESC);
            assertThat(applicationChargeItem.getPennyAmount()).isEqualTo(FEE_ITEM_AMOUNT);
          });
    });


  }

  @Test
  public void acceptApplication_success_paymentRequired() {

    initialReviewService.acceptApplication(
        detail,
        caseOfficerPerson.getId(),
        InitialReviewPaymentDecision.PAYMENT_REQUIRED,
        null,
        pwaManagerUser);

    verify(detailService, times(1)).setInitialReviewApproved(detail, pwaManagerUser, InitialReviewPaymentDecision.PAYMENT_REQUIRED);
    verify(camundaWorkflowService, times(1))
        .completeTask(eq(new WorkflowTaskInstance(app, PwaApplicationWorkflowTask.APPLICATION_REVIEW)));
    verify(camundaWorkflowService, times(1))
        .setWorkflowProperty(
            detail.getPwaApplication(),
            InitialReviewPaymentDecision.PAYMENT_REQUIRED.getPwaApplicationInitialReviewResult()
        );

    verify(applicationChargeRequestService, times(1)).createPwaAppChargeRequest(eq(pwaManagerPerson), chargeRequestSpecCaptor.capture());
    assertThat(chargeRequestSpecCaptor.getValue()).satisfies(requestSpecification -> {

      assertThat(requestSpecification.getPwaApplication()).isEqualTo(app);
      assertThat(requestSpecification.getPwaAppChargeRequestStatus()).isEqualTo(PwaAppChargeRequestStatus.OPEN);
      assertThat(requestSpecification.getChargeSummary()).isEqualTo(FEE_REPORT_SUMMARY);
      assertThat(requestSpecification.getTotalPennies()).isEqualTo(FEE_REPORT_AMOUNT);
      assertThat(requestSpecification.getChargeWaivedReason()).isNull();
      assertThat(requestSpecification.getOnPaymentCompleteCaseOfficerPersonId()).isEqualTo(caseOfficerPerson.getId());
      assertThat(requestSpecification.getApplicationChargeItems()).hasOnlyOneElementSatisfying(applicationChargeItem -> {
        assertThat(applicationChargeItem.getDescription()).isEqualTo(FEE_ITEM_DESC);
        assertThat(applicationChargeItem.getPennyAmount()).isEqualTo(FEE_ITEM_AMOUNT);
      });
    });

    verifyNoInteractions(assignCaseOfficerService);

  }

  @Test
  public void acceptApplication_success_multipleFeeReportFeeItems() {

    applicationFeeReport = ApplicationFeeReportTestUtil.createReport(
        app,
        FEE_REPORT_AMOUNT,
        FEE_REPORT_SUMMARY,
        List.of(
            ApplicationFeeReportTestUtil.createApplicationFeeItem(FEE_ITEM_DESC+"1", FEE_ITEM_AMOUNT),
            ApplicationFeeReportTestUtil.createApplicationFeeItem(FEE_ITEM_DESC+"2", FEE_ITEM_AMOUNT)
        )
    );
    when(applicationFeeService.getApplicationFeeReport(detail)).thenReturn(applicationFeeReport);

    initialReviewService.acceptApplication(
        detail,
        caseOfficerPerson.getId(),
        InitialReviewPaymentDecision.PAYMENT_REQUIRED,
        null,
        pwaManagerUser);


    verify(applicationChargeRequestService, times(1)).createPwaAppChargeRequest(eq(pwaManagerPerson), chargeRequestSpecCaptor.capture());

    assertThat(chargeRequestSpecCaptor.getValue().getApplicationChargeItems())
        .hasSize(2)
        .anySatisfy(applicationChargeItem -> {
          assertThat(applicationChargeItem.getDescription()).isEqualTo(FEE_ITEM_DESC+"1");
          assertThat(applicationChargeItem.getPennyAmount()).isEqualTo(FEE_ITEM_AMOUNT);
        })
        .anySatisfy(applicationChargeItem -> {
          assertThat(applicationChargeItem.getDescription()).isEqualTo(FEE_ITEM_DESC+"2");
          assertThat(applicationChargeItem.getPennyAmount()).isEqualTo(FEE_ITEM_AMOUNT);
        });

  }

  @Test(expected = ActionAlreadyPerformedException.class)
  public void acceptApplication_failed_alreadyAccepted() {

    initialReviewService.acceptApplication(
        detail,
        caseOfficerPerson.getId(),
        InitialReviewPaymentDecision.PAYMENT_REQUIRED,
        null,
        pwaManagerUser);

    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    initialReviewService.acceptApplication(
        detail,
        caseOfficerPerson.getId(),
        InitialReviewPaymentDecision.PAYMENT_REQUIRED,
        null,
        pwaManagerUser);

  }

  @Test(expected = WorkflowAssignmentException.class)
  public void acceptApplication_paymentWaived_invalidCaseOfficer() {

    doThrow(new WorkflowAssignmentException("")).when(assignCaseOfficerService).assignCaseOfficer(any(), any(), any());

    initialReviewService.acceptApplication(
        detail,
        new PersonId(999),
        InitialReviewPaymentDecision.PAYMENT_WAIVED,
        WAIVE_REASON,
        pwaManagerUser
    );

  }

  @Test
  public void canShowInTaskList_ogaCaseManagementPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA), null, null);

    boolean canShow = initialReviewService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null);

    boolean canShow = initialReviewService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = initialReviewService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_initialReviewCompleted_latestDetailReviewed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setInitialReviewApprovedTimestamp(Instant.now());

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    when(detailService.getAllSubmittedApplicationDetailsForApplication(any())).thenReturn(List.of(detail));

    var taskListEntry = initialReviewService.getTaskListEntry(PwaAppProcessingTask.INITIAL_REVIEW, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.COMPLETED));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_previousDetailReviewed_currentDetailNotReviewed() {

    var previousDetailInitialReviewed = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    previousDetailInitialReviewed.setVersionNo(1);
    previousDetailInitialReviewed.setTipFlag(false);
    previousDetailInitialReviewed.setInitialReviewApprovedTimestamp(Instant.now());

    var currentDetailNotReviewed = new PwaApplicationDetail(
        previousDetailInitialReviewed.getPwaApplication(),
        2,
        null, null);
    currentDetailNotReviewed.setVersionNo(2);

    var processingContext = new PwaAppProcessingContext(currentDetailNotReviewed, null, Set.of(), null, null);

    when(detailService.getAllSubmittedApplicationDetailsForApplication(any())).thenReturn(List.of(currentDetailNotReviewed, previousDetailInitialReviewed));

    var taskListEntry = initialReviewService.getTaskListEntry(PwaAppProcessingTask.INITIAL_REVIEW, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.COMPLETED));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
  }

  @Test
  public void getTaskListEntry_initialReviewNotCompleted_noAcceptInitialReviewPermission() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    var taskListEntry = initialReviewService.getTaskListEntry(PwaAppProcessingTask.INITIAL_REVIEW, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_initialReviewNotCompleted_whenAcceptInitialReviewPermission() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW), null, null);

    var taskListEntry = initialReviewService.getTaskListEntry(PwaAppProcessingTask.INITIAL_REVIEW, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_appUpdateRequestOpen() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail)).thenReturn(true);

    var taskListEntry = initialReviewService.getTaskListEntry(PwaAppProcessingTask.INITIAL_REVIEW, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

}
