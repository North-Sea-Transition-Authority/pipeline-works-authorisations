package uk.co.ogauthority.pwa.service.consultations;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.assignments.ApplicationAssignedToYouEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.assignments.CaseOfficerAssignedEmailProps;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.AssignCaseOfficerForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.consultations.AssignCaseOfficerValidator;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssignCaseOfficerServiceTest {

  private AssignCaseOfficerService assignCaseOfficerService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private PersonService personService;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private AssignCaseOfficerValidator assignCaseOfficerValidator;

  @Mock
  private EmailService emailService;

  private PwaApplicationDetail appDetail;

  @Captor
  private ArgumentCaptor<CaseOfficerAssignedEmailProps> caseOfficerAssignedEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<ApplicationAssignedToYouEmailProps> applicationAssignedToYouEmailPropsCaptor;

  private Person assigningPerson;
  private AuthenticatedUserAccount assigningUser;
  private Person caseOfficerPerson;
  private static final String CASE_LINK = "case link";

  @BeforeEach
  void setUp() {

    assignCaseOfficerService = new AssignCaseOfficerService(
        workflowAssignmentService,
        personService,
        assignCaseOfficerValidator,
        caseLinkService,
        emailService);
    appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 1);

    assigningPerson = new Person(1, "m", "assign", "assign@assign.com", null);
    when(personService.getPersonById(assigningPerson.getId())).thenReturn(assigningPerson);
    appDetail.setSubmittedByPersonId(assigningPerson.getId());

    assigningUser = new AuthenticatedUserAccount(
        new WebUserAccount(1, assigningPerson), null);

    caseOfficerPerson = new Person(2, "fore", "sur", "fore@sur.com", null);
    when(personService.getPersonById(caseOfficerPerson.getId())).thenReturn(caseOfficerPerson);


    when(caseLinkService.generateCaseManagementLink(appDetail.getPwaApplication())).thenReturn(CASE_LINK);

  }

  @Test
  void assignCaseOfficer_assignToDifferentUser_emailSent() {

    var form = new AssignCaseOfficerForm();
    form.setCaseOfficerPersonId(2);

    assignCaseOfficerService.assignCaseOfficer(appDetail, form.getCaseOfficerPerson(), assigningUser);

    //verify new case officer assignment done and email is sent
    verify(workflowAssignmentService).assign(
        appDetail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, caseOfficerPerson, assigningUser.getLinkedPerson());

    verifyEmailsSentAndPropsPopulatedCorrectly();

  }

  private void verifyEmailsSentAndPropsPopulatedCorrectly() {
    verify(emailService).sendEmail(caseOfficerAssignedEmailPropsCaptor.capture(), eq(assigningUser.getLinkedPerson()), eq(appDetail.getPwaApplicationRef()));
    verify(emailService).sendEmail(applicationAssignedToYouEmailPropsCaptor.capture(), eq(caseOfficerPerson), eq(appDetail.getPwaApplicationRef()));

    var caseOfficerAssignedProps = caseOfficerAssignedEmailPropsCaptor.getValue();
    assertThat(caseOfficerAssignedProps.getEmailPersonalisation()).contains(
        entry("RECIPIENT_FULL_NAME", assigningUser.getLinkedPerson().getFullName()),
        entry("CASE_OFFICER_NAME", caseOfficerPerson.getFullName()),
        entry("APPLICATION_REFERENCE", appDetail.getPwaApplicationRef()),
        entry("CASE_MANAGEMENT_LINK", CASE_LINK)
    );

    var applicationAssignedToYouProps = applicationAssignedToYouEmailPropsCaptor.getValue();
    assertThat(applicationAssignedToYouProps.getEmailPersonalisation()).contains(
        entry("RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName()),
        entry("ASSIGNING_PERSON_FULL_NAME", assigningPerson.getFullName()),
        entry("APPLICATION_REFERENCE", appDetail.getPwaApplicationRef()),
        entry("CASE_MANAGEMENT_LINK", "case link")
    );
  }

  @Test
  void autoAssignCaseOfficer_success() {

    when(workflowAssignmentService.assignTaskNoException(
        appDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson,
        assigningPerson
    )).thenReturn(WorkflowAssignmentService.AssignTaskResult.SUCCESS);

    assignCaseOfficerService.autoAssignCaseOfficer(appDetail, caseOfficerPerson, assigningPerson);

    verify(workflowAssignmentService).assignTaskNoException(
        appDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson,
        assigningPerson
    );

    verifyEmailsSentAndPropsPopulatedCorrectly();

  }

  @Test
  void autoAssignCaseOfficer_failure() {

    when(workflowAssignmentService.assignTaskNoException(
        appDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson,
        assigningPerson
    )).thenReturn(WorkflowAssignmentService.AssignTaskResult.ASSIGNMENT_CANDIDATE_INVALID);

    assignCaseOfficerService.autoAssignCaseOfficer(appDetail, caseOfficerPerson, assigningPerson);

    verifyNoInteractions(emailService);

  }

  @Test
  void canShowInTaskList_hasPermission() {
    appDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var processingContext = new PwaAppProcessingContext(appDetail, null, Set.of(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER), null, null,
        Set.of());

    boolean canShow = assignCaseOfficerService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_noPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null, Set.of());

    boolean canShow = assignCaseOfficerService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void canShowInTaskList_hasPermissionWithIncorrectStatus() {
    appDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    var processingContext = PwaAppProcessingContextTestUtil.withPermissions(appDetail, Set.of(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER));

    boolean canShow = assignCaseOfficerService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void getTaskListEntry_invalidPermission_taskLocked() {
    appDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var processingContext = PwaAppProcessingContextTestUtil.withoutPermissions(appDetail);

    var taskListEntry = assignCaseOfficerService.getTaskListEntry(PwaAppProcessingTask.ALLOCATE_CASE_OFFICER, processingContext);

    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);

  }

  @Test
  void getTaskListEntry_invalidAppStatus_taskLocked() {
    appDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    var processingContext = PwaAppProcessingContextTestUtil.withPermissions(appDetail, Set.of(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER));

    var taskListEntry = assignCaseOfficerService.getTaskListEntry(PwaAppProcessingTask.ALLOCATE_CASE_OFFICER, processingContext);

    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);

  }

  @Test
  void getTaskListEntry_validPermissionAndAppStatus_taskEditable() {
    appDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var processingContext = PwaAppProcessingContextTestUtil.withPermissions(appDetail, Set.of(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER));

    var taskListEntry = assignCaseOfficerService.getTaskListEntry(PwaAppProcessingTask.ALLOCATE_CASE_OFFICER, processingContext);

    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);

  }


}

