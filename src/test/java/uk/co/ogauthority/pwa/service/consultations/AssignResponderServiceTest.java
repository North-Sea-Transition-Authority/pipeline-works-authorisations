package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ConsultationAssignedToYouEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidationHints;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidator;


@RunWith(MockitoJUnitRunner.class)
public class AssignResponderServiceTest {

  private AssignResponderService assignResponderService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @Mock
  private AssignResponderValidator validator;

  @Mock
  private TeamManagementService teamManagementService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Captor
  private ArgumentCaptor<ConsultationAssignedToYouEmailProps> emailPropsCaptor;

  @Before
  public void setUp() {
    assignResponderService = new AssignResponderService(
        workflowAssignmentService,
        validator,
        consulteeGroupTeamService,
        teamManagementService,
        camundaWorkflowService,
        consultationRequestService,
        notifyService,
        emailCaseLinkService);
  }

  @Test
  public void getAllRespondersForRequest() {
    ConsultationRequest consultationRequest = new ConsultationRequest();
    var expectedResponder1 = new Person(1, "", "Smith", "", "");
    var expectedResponder2 = new Person(2, "", "Berry", "", "");
    when(workflowAssignmentService.getAssignmentCandidates(consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE)).thenReturn(Set.of(expectedResponder1, expectedResponder2));

    List<Person> responders = assignResponderService.getAllRespondersForRequest(consultationRequest);
    assertThat(responders.get(0)).isEqualTo(expectedResponder2);
    assertThat(responders.get(1)).isEqualTo(expectedResponder1);
  }

  @Test
  public void assignUserAndCompleteWorkflow_allocation_workflowProgressed_assignToDifferentUser_emailSent() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var app = new PwaApplication();
    app.setAppReference("PA/2/2");
    consultationRequest.setPwaApplication(app);
    var deadline = Instant.now().plusSeconds(86400);
    consultationRequest.setDeadlineDate(deadline);
    consultationRequest.setStatus(ConsultationRequestStatus.ALLOCATION);

    var form = new AssignResponderForm();
    form.setResponderPersonId(2);

    var assigningUser = new WebUserAccount(1, new Person(1, "m", "assign", "assign@assign.com", null));

    var responderPerson = new Person(2, "fore", "sur", "fore@sur.com", null);
    when(teamManagementService.getPerson(2)).thenReturn(responderPerson);

    var task = new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION);
    when(camundaWorkflowService.getAllActiveWorkflowTasks(consultationRequest))
        .thenReturn(Set.of(task));

    var caseManagementLink = "case link";
    when(emailCaseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication())).thenReturn(caseManagementLink);

    assignResponderService.assignResponder(form, consultationRequest, assigningUser);

    verify(camundaWorkflowService, times(1)).completeTask(eq(task));

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        assigningUser.getLinkedPerson()
    );

    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(responderPerson.getEmailAddress()));

    var emailProps = emailPropsCaptor.getValue();

    assertThat(emailProps.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("RECIPIENT_FULL_NAME", responderPerson.getFullName()),
            tuple("APPLICATION_REFERENCE", app.getAppReference()),
            tuple("ASSIGNER_FULL_NAME", assigningUser.getLinkedPerson().getFullName()),
            tuple("DUE_DATE", DateUtils.formatDate(deadline)),
            tuple("CASE_MANAGEMENT_LINK", caseManagementLink)
        );

    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.AWAITING_RESPONSE);
  }

  @Test
  public void assignUserAndCompleteWorkflow_allocation_workflowProgressed_assigningToSelf_noEmailSent() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var app = new PwaApplication();
    app.setAppReference("PA/2/2");
    consultationRequest.setPwaApplication(app);
    var deadline = Instant.now().plusSeconds(86400);
    consultationRequest.setDeadlineDate(deadline);
    consultationRequest.setStatus(ConsultationRequestStatus.ALLOCATION);

    var form = new AssignResponderForm();
    form.setResponderPersonId(1);

    var assigningUser = new WebUserAccount(1, new Person(1, "m", "assign", "assign@assign.com", null));

    when(teamManagementService.getPerson(1)).thenReturn(assigningUser.getLinkedPerson());

    var task = new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION);
    when(camundaWorkflowService.getAllActiveWorkflowTasks(consultationRequest))
        .thenReturn(Set.of(task));

    assignResponderService.assignResponder(form, consultationRequest, assigningUser);

    verify(camundaWorkflowService, times(1)).completeTask(task);

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        assigningUser.getLinkedPerson(),
        assigningUser.getLinkedPerson()
    );

    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);

    verifyNoInteractions(notifyService);

    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.AWAITING_RESPONSE);

  }

  @Test
  public void assignUserAndCompleteWorkflow_response_workflowStageDoesntChange_assignToDifferentUser_emailSent() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var app = new PwaApplication();
    app.setAppReference("PA/2/2");
    consultationRequest.setPwaApplication(app);
    var deadline = Instant.now().plusSeconds(86400);
    consultationRequest.setDeadlineDate(deadline);
    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);

    var form = new AssignResponderForm();
    form.setResponderPersonId(2);

    var assigningUser = new WebUserAccount(1, new Person(1, "m", "assign", "assign@assign.com", null));

    var responderPerson = new Person(2, "fore", "sur", "fore@sur.com", null);
    when(teamManagementService.getPerson(2)).thenReturn(responderPerson);

    var task = new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE);
    when(camundaWorkflowService.getAllActiveWorkflowTasks(consultationRequest))
        .thenReturn(Set.of(task));

    assignResponderService.assignResponder(form, consultationRequest, assigningUser);

    verify(camundaWorkflowService, times(0)).completeTask(eq(task));

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        assigningUser.getLinkedPerson()
    );

    verify(consultationRequestService, times(0)).saveConsultationRequest(consultationRequest);

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(responderPerson.getEmailAddress()));

    var emailProps = emailPropsCaptor.getValue();

    assertThat(emailProps.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("RECIPIENT_FULL_NAME", responderPerson.getFullName()),
            tuple("APPLICATION_REFERENCE", app.getAppReference()),
            tuple("ASSIGNER_FULL_NAME", assigningUser.getLinkedPerson().getFullName()),
            tuple("DUE_DATE", DateUtils.formatDate(deadline))
        );

    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.AWAITING_RESPONSE);
  }

  @Test
  public void assignUserAndCompleteWorkflow_response_workflowStageDoesntChange_assigningToSelf_noEmailSent() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var app = new PwaApplication();
    app.setAppReference("PA/2/2");
    consultationRequest.setPwaApplication(app);
    var deadline = Instant.now().plusSeconds(86400);
    consultationRequest.setDeadlineDate(deadline);
    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);

    var form = new AssignResponderForm();
    form.setResponderPersonId(1);

    var assigningUser = new WebUserAccount(1, new Person(1, "m", "assign", "assign@assign.com", null));

    when(teamManagementService.getPerson(1)).thenReturn(assigningUser.getLinkedPerson());

    var task = new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE);
    when(camundaWorkflowService.getAllActiveWorkflowTasks(consultationRequest))
        .thenReturn(Set.of(task));

    assignResponderService.assignResponder(form, consultationRequest, assigningUser);

    verify(camundaWorkflowService, times(0)).completeTask(eq(task));

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        assigningUser.getLinkedPerson(),
        assigningUser.getLinkedPerson()
    );

    verify(consultationRequestService, times(0)).saveConsultationRequest(consultationRequest);

    verifyNoInteractions(notifyService);

    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.AWAITING_RESPONSE);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void assignUserAndCompleteWorkflow_personNotFound() {

    when(teamManagementService.getPerson(5)).thenThrow(new PwaEntityNotFoundException(""));

    var form = new AssignResponderForm();
    form.setResponderPersonId(5);

    assignResponderService.assignResponder(form, new ConsultationRequest(), new WebUserAccount());
  }

  @Test
  public void validate() {
    var form = new AssignResponderForm();
    assignResponderService.validate(form, new BeanPropertyBindingResult(form, "form"), new ConsultationRequest());
    verify(validator, times(1)).validate(any(AssignResponderForm.class), any(BeanPropertyBindingResult.class), any(
        AssignResponderValidationHints.class));
  }

  @Test
  public void isUserMemberOfRequestGroup_valid() {
    var usersGroup = new ConsulteeGroup();
    usersGroup.setId(1);

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember();
    consulteeGroupTeamMember.setConsulteeGroup(usersGroup);
    consulteeGroupTeamMember.setRoles(Set.of(ConsulteeGroupMemberRole.RESPONDER));

    var user = new WebUserAccount(1);
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setConsulteeGroup(usersGroup);

    when(consulteeGroupTeamService.getTeamMemberByPerson(user.getLinkedPerson())).thenReturn(Optional.of(consulteeGroupTeamMember));
    boolean isMemberOfRequestGroup = assignResponderService.isUserMemberOfRequestGroup(user, consultationRequest);

    assertTrue(isMemberOfRequestGroup);
  }

  @Test
  public void isUserMemberOfRequestGroup_invalid() {
    var usersGroup = new ConsulteeGroup();
    usersGroup.setId(1);

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember();
    consulteeGroupTeamMember.setConsulteeGroup(usersGroup);
    consulteeGroupTeamMember.setRoles(Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    var user = new WebUserAccount(1);
    var consultationRequest = new ConsultationRequest();
    var requestGroup = new ConsulteeGroup();
    requestGroup.setId(2);
    consultationRequest.setConsulteeGroup(requestGroup);

    when(consulteeGroupTeamService.getTeamMemberByPerson(user.getLinkedPerson())).thenReturn(Optional.of(consulteeGroupTeamMember));
    boolean isMemberOfRequestGroup = assignResponderService.isUserMemberOfRequestGroup(user, consultationRequest);

    assertFalse(isMemberOfRequestGroup);
  }

  @Test
  public void canShowInTaskList_hasPermission_noActiveConsultation_notShown() {

    var processingContext = new PwaAppProcessingContext(
        null,
        null,
        Set.of(PwaAppProcessingPermission.ASSIGN_RESPONDER),
        null,
        new ApplicationInvolvementDto(null, Set.of(), new ConsultationInvolvementDto(null, Set.of(), null, List.of(), false), false,
            false, false, EnumSet.noneOf(PwaOrganisationRole.class)));

    boolean canShow = assignResponderService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_hasPermission_activeConsultation_shown() {

    var processingContext = new PwaAppProcessingContext(
        null,
        null,
        Set.of(PwaAppProcessingPermission.ASSIGN_RESPONDER),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", new ConsultationRequest()));

    boolean canShow = assignResponderService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = assignResponderService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_noOneAssignedYet() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var request = new ConsultationRequest();
    var processingContext = new PwaAppProcessingContext(
        detail,
        null,
        Set.of(),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request));

    var taskListEntry = assignResponderService.getTaskListEntry(PwaAppProcessingTask.ALLOCATE_RESPONDER, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_COMPLETED));
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getDisplayOrder());

  }

  @Test
  public void getTaskListEntry_personAssigned() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var request = new ConsultationRequest();
    var processingContext = new PwaAppProcessingContext(
        detail,
        null,
        Set.of(),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request));

    var taskInstance = new WorkflowTaskInstance(request, PwaApplicationConsultationWorkflowTask.RESPONSE);
    when(camundaWorkflowService.getAllActiveWorkflowTasks(request)).thenReturn(Set.of(taskInstance));

    var person = new Person(1, "fore", "sur", null, null);
    when(workflowAssignmentService.getAssignee(taskInstance)).thenReturn(Optional.of(person));

    var taskListEntry = assignResponderService.getTaskListEntry(PwaAppProcessingTask.ALLOCATE_RESPONDER, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualTo(person.getFullName());
    assertThat(taskListEntry.getTaskTag().getTagClass()).isEqualTo("govuk-tag--purple");
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getDisplayOrder());

  }

  @Test(expected = RuntimeException.class)
  public void getTaskListEntry_noActiveConsultationRequest() {

    var processingContext = new PwaAppProcessingContext(null, null, null, null, null);

    assignResponderService.getTaskListEntry(PwaAppProcessingTask.ALLOCATE_RESPONDER, processingContext);

  }

}

