package uk.co.ogauthority.pwa.service.workflow.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.AssignmentService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.GenericMessageEvent;
import uk.co.ogauthority.pwa.integrations.camunda.external.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.integrations.camunda.external.UserWorkflowTask;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.OldTeamManagementService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowAssignmentServiceTest {

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private AssignmentService assignmentService;

  @Mock
  private PwaTeamService pwaTeamService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private OldTeamManagementService teamManagementService;

  @InjectMocks

  private WorkflowAssignmentService workflowAssignmentService;

  private Person caseOfficerPerson, notCaseOfficerPerson;

  private GenericWorkflowSubject pwaApplicationSubject;
  private GenericWorkflowSubject consultationSubject;

  private ConsultationRequest consultationRequest;
  private ConsulteeGroupDetail consulteeGroupDetail;

  @BeforeEach
  void setUp() {

    caseOfficerPerson = new Person(1, null, null, null, null);
    notCaseOfficerPerson = new Person(2, null, null, null, null);

    when(pwaTeamService.getPeopleWithRegulatorRole(Role.CASE_OFFICER)).thenReturn(Set.of(caseOfficerPerson));

    pwaApplicationSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    consultationSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION_CONSULTATION);

    consultationRequest = new ConsultationRequest();
    consulteeGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("TEST", "T");
    consultationRequest.setConsulteeGroup(consulteeGroupDetail.getConsulteeGroup());

    when(consultationRequestService.getConsultationRequestByIdOrThrow(eq(consultationSubject.getBusinessKey()))).thenReturn(
        consultationRequest);

  }

  @Test
  void getAssignmentCandidates_caseOfficer() {

    assertThat(workflowAssignmentService.getAssignmentCandidates(pwaApplicationSubject,
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)).containsOnly(caseOfficerPerson);

  }

  @Test
  void getAssignmentCandidates_consultationResponder_respondersExist() {

    var person1 = new Person(1, null, null, null, null);

    when(pwaTeamService.getPeopleByConsulteeGroupAndRoleIn(eq(consulteeGroupDetail.getConsulteeGroup()), anySet()))
        .thenReturn(Set.of(person1));

    var responderPeople = workflowAssignmentService.getAssignmentCandidates(consultationSubject,
        PwaApplicationConsultationWorkflowTask.RESPONSE);

    assertThat(responderPeople).containsOnly(person1);

  }

  @Test
  void getAssignmentCandidates_consultationResponder_noResponders() {

    when(pwaTeamService.getPeopleByConsulteeGroupAndRoleIn(eq(consulteeGroupDetail.getConsulteeGroup()), anySet()))
        .thenReturn(Set.of());

    assertThat(workflowAssignmentService.getAssignmentCandidates(consultationSubject,
        PwaApplicationConsultationWorkflowTask.RESPONSE)).isEmpty();

  }

  @Test
  void getAssignmentCandidates_consultationResponder_invalidWorkflow() {
    assertThrows(IllegalArgumentException.class, () ->

      workflowAssignmentService.getAssignmentCandidates(pwaApplicationSubject,
          PwaApplicationConsultationWorkflowTask.RESPONSE));

  }

  @Test
  void getAssignmentCandidates_noAssignment() {

    assertThat(workflowAssignmentService.getAssignmentCandidates(pwaApplicationSubject,
        PwaApplicationWorkflowTask.PREPARE_APPLICATION)).isEmpty();

  }

  @Test
  void assign_success() {

    var app = new PwaApplication();

    workflowAssignmentService.assign(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson, notCaseOfficerPerson);

    verify(camundaWorkflowService, times(1)).assignTaskToUser(
        new WorkflowTaskInstance(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW),
        caseOfficerPerson);

    verify(assignmentService, times(1))
        .createOrUpdateAssignment(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, caseOfficerPerson, notCaseOfficerPerson);

  }

  @Test
  void assign_invalid() {
    var app = new PwaApplication();
    assertThrows(WorkflowAssignmentException.class, () ->

      workflowAssignmentService.assign(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, notCaseOfficerPerson,
          caseOfficerPerson));

  }

  @Test
  void assignTaskNoException_success() {

    var app = new PwaApplication();

    assertThat(workflowAssignmentService.assignTaskNoException(
       app,
       PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson,
       notCaseOfficerPerson)
    ).isEqualTo(WorkflowAssignmentService.AssignTaskResult.SUCCESS);

    verify(camundaWorkflowService, times(1)).assignTaskToUser(
        eq(new WorkflowTaskInstance(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)),
        eq(caseOfficerPerson));

    verify(assignmentService, times(1))
        .createOrUpdateAssignment(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, caseOfficerPerson, notCaseOfficerPerson);

  }

  @Test
  void assignTaskNoException_invalidAssigneePerson() {

    var app = new PwaApplication();

    assertThat(workflowAssignmentService.assignTaskNoException(
        app,
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        notCaseOfficerPerson,
        notCaseOfficerPerson)
    ).isEqualTo(WorkflowAssignmentService.AssignTaskResult.ASSIGNMENT_CANDIDATE_INVALID);

    verify(camundaWorkflowService, times(0)).assignTaskToUser(any(), any());
    verify(assignmentService, times(0)).createOrUpdateAssignment(any(), any(), any(), any());

  }

  @Test
  void triggerWorkflowMessageAndAssertTaskExists_whenExpectedTaskExists() {
    var testMessageName = "TEST";
    var testTaskKey = "TASK_KEY";
    var mockWorkFlowTask = mock(UserWorkflowTask.class);
    when(mockWorkFlowTask.getTaskKey()).thenReturn(testTaskKey);
    var mockTaskInstance  = mock(WorkflowTaskInstance.class);
    when(mockTaskInstance.getTaskKey()).thenReturn(testTaskKey);

    when(camundaWorkflowService.getAllActiveWorkflowTasks(pwaApplicationSubject)).thenReturn(Set.of(mockTaskInstance));

    var genericMessageEvent = GenericMessageEvent.from(pwaApplicationSubject, testMessageName);
    workflowAssignmentService.triggerWorkflowMessageAndAssertTaskExists(genericMessageEvent, mockWorkFlowTask);

    verify(camundaWorkflowService, times(1)).triggerMessageEvent(pwaApplicationSubject, testMessageName);
    verify(camundaWorkflowService, times(1)).getAllActiveWorkflowTasks(pwaApplicationSubject);
  }

  @Test
  void triggerWorkflowMessageAndAssertTaskExists_whenExpectedTaskDoesNotExists() {
    var testMessageName = "TEST";
    var mockWorkFlowTask = mock(UserWorkflowTask.class);
    when(camundaWorkflowService.getAllActiveWorkflowTasks(pwaApplicationSubject)).thenReturn(Set.of());
    var genericMessageEvent = GenericMessageEvent.from(pwaApplicationSubject, testMessageName);
    assertThrows(WorkflowAssignmentException.class, () ->
      workflowAssignmentService.triggerWorkflowMessageAndAssertTaskExists(genericMessageEvent, mockWorkFlowTask));

  }

  @Test
  void getAssignee_assigneeExists_personExists_fullOptional() {

    var person = new Person(1, null, null, null, null);

    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.of(new PersonId(1)));
    when(teamManagementService.getPerson(1)).thenReturn(person);

    var taskInstance = new WorkflowTaskInstance(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);

    var retrievedPerson = workflowAssignmentService.getAssignee(taskInstance);

    assertThat(retrievedPerson).contains(person);

  }

  @Test
  void getAssignee_assigneeExists_personDoesntExist_error() {
    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.of(new PersonId(1)));
    when(teamManagementService.getPerson(1)).thenThrow(PwaEntityNotFoundException.class);
    var taskInstance = new WorkflowTaskInstance(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);
    assertThrows(PwaEntityNotFoundException.class, () ->

      workflowAssignmentService.getAssignee(taskInstance));

  }

  @Test
  void getAssignee_assigneeDoesntExist_emptyOptional() {


    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.empty());

    var taskInstance = new WorkflowTaskInstance(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);

    var retrievedPerson = workflowAssignmentService.getAssignee(taskInstance);

    assertThat(retrievedPerson).isEmpty();

  }

  @Test
  void clearAssignments_verifyServiceInteractions() {

    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    workflowAssignmentService.clearAssignments(workflowSubject);

    verify(assignmentService, times(1)).clearAssignments(workflowSubject);

  }

}
