package uk.co.ogauthority.pwa.service.appprocessing.initialreview;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class InitialReviewServiceTest {

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private WorkflowAssignmentService assignmentService;

  @Mock
  private TeamManagementService teamManagementService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private PersonService personService;

  @Captor
  private ArgumentCaptor<EmailProperties> emailPropertiesArgumentCaptor;

  private InitialReviewService initialReviewService;

  private PwaApplicationDetail detail;

  private PwaApplication app;

  private Person industryPerson;
  private WebUserAccount industryUser;

  private Person caseOfficerPerson;

  @Before
  public void setUp() {

    industryPerson = new Person(1, "Industry", "Person", "industry@pwa.co.uk", null);
    industryUser = new WebUserAccount(1, industryPerson);

    app = new PwaApplication();
    app.setId(1);
    app.setAppReference("PA/2/1");

    detail = new PwaApplicationDetail();
    detail.setPwaApplication(app);
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    detail.setSubmittedByPersonId(industryPerson.getId());

    caseOfficerPerson = new Person(555, "Test", "CO", "case-officer@pwa.co.uk", null);

    when(personService.getPersonById(industryPerson.getId())).thenReturn(industryPerson);

    when(teamManagementService.getPerson(caseOfficerPerson.getId().asInt())).thenReturn(caseOfficerPerson);

    initialReviewService = new InitialReviewService(
        detailService,
        camundaWorkflowService,
        assignmentService,
        teamManagementService,
        notifyService,
        personService);

  }

  @Test
  public void acceptApplication_success() {

    initialReviewService.acceptApplication(detail, caseOfficerPerson.getId().asInt(), industryUser);

    verify(detailService, times(1)).setInitialReviewApproved(detail, industryUser);
    verify(camundaWorkflowService, times(1))
        .completeTask(eq(new WorkflowTaskInstance(app, PwaApplicationWorkflowTask.APPLICATION_REVIEW)));

    verify(assignmentService, times(1)).assign(
        app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, caseOfficerPerson, industryUser.getLinkedPerson());

    verify(notifyService, times(1)).sendEmail(emailPropertiesArgumentCaptor.capture(), eq(industryPerson.getEmailAddress()));

    var emailProps = emailPropertiesArgumentCaptor.getValue();

    assertThat(emailProps.getEmailPersonalisation()).contains(
        entry("RECIPIENT_FULL_NAME", industryPerson.getFullName()),
        entry("CASE_OFFICER_NAME", caseOfficerPerson.getFullName()),
        entry("APPLICATION_REFERENCE", detail.getPwaApplicationRef())
    );

  }

  @Test(expected = ActionAlreadyPerformedException.class)
  public void acceptApplication_failed_alreadyAccepted() {

    initialReviewService.acceptApplication(detail, caseOfficerPerson.getId().asInt(), industryUser);

    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    initialReviewService.acceptApplication(detail, caseOfficerPerson.getId().asInt(), industryUser);

  }

  @Test(expected = WorkflowAssignmentException.class)
  public void acceptApplication_invalidCaseOfficer() {

    doThrow(new WorkflowAssignmentException("")).when(assignmentService).assign(any(), any(), any(), any());

    initialReviewService.acceptApplication(detail, 999, industryUser);

  }

  @Test
  public void canShowInTaskList_initialReviewPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW), null, null);

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
  public void getTaskListEntry_initialReviewCompleted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setInitialReviewApprovedTimestamp(Instant.now());

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    var taskListEntry = initialReviewService.getTaskListEntry(PwaAppProcessingTask.INITIAL_REVIEW, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.COMPLETED));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_initialReviewNotCompleted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    var taskListEntry = initialReviewService.getTaskListEntry(PwaAppProcessingTask.INITIAL_REVIEW, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_COMPLETED));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

}
