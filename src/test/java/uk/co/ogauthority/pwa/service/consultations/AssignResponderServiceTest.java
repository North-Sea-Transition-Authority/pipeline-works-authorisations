package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationAssignedToYouEmailProps;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.OldTeamManagementService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidationHints;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidator;


@ExtendWith(MockitoExtension.class)
class AssignResponderServiceTest {

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private AssignResponderValidator validator;

  @Mock
  private OldTeamManagementService teamManagementService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private EmailService emailService;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private AssignResponderService assignResponderService;

  @Captor
  private ArgumentCaptor<ConsultationAssignedToYouEmailProps> emailPropsCaptor;

  @Test
  void getAllRespondersForRequest() {
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
  void assignUserAndCompleteWorkflow_allocation_workflowProgressed_assignToDifferentUser_emailSent() {

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
    when(caseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication())).thenReturn(caseManagementLink);

    assignResponderService.assignResponder(form, consultationRequest, assigningUser);

    verify(camundaWorkflowService, times(1)).completeTask(task);

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        assigningUser.getLinkedPerson()
    );

    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);

    verify(emailService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(responderPerson),
        eq(consultationRequest.getPwaApplication().getAppReference()));

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
  void assignUserAndCompleteWorkflow_allocation_workflowProgressed_assigningToSelf_noEmailSent() {

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

    verifyNoInteractions(emailService);

    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.AWAITING_RESPONSE);

  }

  @Test
  void assignUserAndCompleteWorkflow_response_workflowStageDoesntChange_assignToDifferentUser_emailSent() {

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

    verify(camundaWorkflowService, times(0)).completeTask(task);

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        assigningUser.getLinkedPerson()
    );

    verify(consultationRequestService, times(0)).saveConsultationRequest(consultationRequest);

    verify(emailService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(responderPerson),
        eq(consultationRequest.getPwaApplication().getAppReference()));

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
  void assignUserAndCompleteWorkflow_response_workflowStageDoesntChange_assigningToSelf_noEmailSent() {

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

    verify(camundaWorkflowService, times(0)).completeTask(task);

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        assigningUser.getLinkedPerson(),
        assigningUser.getLinkedPerson()
    );

    verify(consultationRequestService, times(0)).saveConsultationRequest(consultationRequest);

    verifyNoInteractions(emailService);

    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.AWAITING_RESPONSE);

  }

  @Test
  void assignUserAndCompleteWorkflow_personNotFound() {
    when(teamManagementService.getPerson(5)).thenThrow(new PwaEntityNotFoundException(""));
    var form = new AssignResponderForm();
    form.setResponderPersonId(5);
    assertThrows(PwaEntityNotFoundException.class, () ->

      assignResponderService.assignResponder(form, new ConsultationRequest(), new WebUserAccount()));
  }

  @Test
  void validate() {
    var form = new AssignResponderForm();
    assignResponderService.validate(form, new BeanPropertyBindingResult(form, "form"), new ConsultationRequest());
    verify(validator, times(1)).validate(any(AssignResponderForm.class), any(BeanPropertyBindingResult.class), any(
        AssignResponderValidationHints.class));
  }

  @Test
  void isUserMemberOfRequestGroup_valid() {
    var user = new WebUserAccount(1);
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(2);
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setConsulteeGroup(consulteeGroup);

    when(teamQueryService.userHasAtLeastOneScopedRole(
        eq((long) user.getWuaId()),
        eq(TeamType.CONSULTEE),
        any(TeamScopeReference.class),
        anySet()
    )).thenReturn(true);

    assertTrue(assignResponderService.isUserMemberOfRequestGroup(user, consultationRequest));
  }

  @Test
  void isUserMemberOfRequestGroup_invalid() {
    var user = new WebUserAccount(1);
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(2);
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setConsulteeGroup(consulteeGroup);

    when(teamQueryService.userHasAtLeastOneScopedRole(
        eq((long) user.getWuaId()),
        eq(TeamType.CONSULTEE),
        any(TeamScopeReference.class),
        anySet()
    )).thenReturn(false);

    assertFalse(assignResponderService.isUserMemberOfRequestGroup(user, consultationRequest));
  }

  @Test
  void canShowInTaskList_hasPermission_noActiveConsultation_notShown() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        null,
        new ConsultationInvolvementDto(null, Set.of(), null, List.of(), false)
    );

    var processingContext = new PwaAppProcessingContext(
        null,
        null,
        Set.of(PwaAppProcessingPermission.ASSIGN_RESPONDER),
        null,
        appInvolvement,
        Set.of());

    boolean canShow = assignResponderService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void canShowInTaskList_hasPermission_activeConsultation_shown() {

    var processingContext = new PwaAppProcessingContext(
        null,
        null,
        Set.of(PwaAppProcessingPermission.ASSIGN_RESPONDER),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", new ConsultationRequest()),
        Set.of());

    boolean canShow = assignResponderService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_noPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null, Set.of());

    boolean canShow = assignResponderService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void getTaskListEntry_noOneAssignedYet() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var request = new ConsultationRequest();
    var processingContext = new PwaAppProcessingContext(
        detail,
        null,
        Set.of(),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request), Set.of());

    var taskListEntry = assignResponderService.getTaskListEntry(PwaAppProcessingTask.ALLOCATE_RESPONDER, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getDisplayOrder());

  }

  @Test
  void getTaskListEntry_personAssigned() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var request = new ConsultationRequest();
    var processingContext = new PwaAppProcessingContext(
        detail,
        null,
        Set.of(),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request), Set.of());

    var taskInstance = new WorkflowTaskInstance(request, PwaApplicationConsultationWorkflowTask.RESPONSE);
    when(camundaWorkflowService.getAllActiveWorkflowTasks(request)).thenReturn(Set.of(taskInstance));

    var person = new Person(1, "fore", "sur", null, null);
    when(workflowAssignmentService.getAssignee(taskInstance)).thenReturn(Optional.of(person));

    var taskListEntry = assignResponderService.getTaskListEntry(PwaAppProcessingTask.ALLOCATE_RESPONDER, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualTo(person.getFullName());
    assertThat(taskListEntry.getTaskTag().getTagClass()).isEqualTo("govuk-tag--purple");
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.ALLOCATE_RESPONDER.getDisplayOrder());

  }

  @Test
  void getTaskListEntry_noActiveConsultationRequest() {
    var processingContext = new PwaAppProcessingContext(null, null, null, null, null, Set.of());
    assertThrows(RuntimeException.class, () ->

      assignResponderService.getTaskListEntry(PwaAppProcessingTask.ALLOCATE_RESPONDER, processingContext));

  }

}

