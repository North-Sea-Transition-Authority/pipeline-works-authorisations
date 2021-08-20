package uk.co.ogauthority.pwa.service.workflow.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.workflow.GenericMessageEvent;
import uk.co.ogauthority.pwa.model.workflow.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowAssignmentServiceTest {

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private AssignmentService assignmentService;

  @Mock
  private PwaTeamService pwaTeamService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @Mock
  private TeamManagementService teamManagementService;

  private WorkflowAssignmentService workflowAssignmentService;

  private Person caseOfficerPerson, notCaseOfficerPerson;

  private GenericWorkflowSubject pwaApplicationSubject;
  private GenericWorkflowSubject consultationSubject;

  private ConsultationRequest consultationRequest;
  private ConsulteeGroupDetail consulteeGroupDetail;

  @Before
  public void setUp() {

    caseOfficerPerson = new Person(1, null, null, null, null);
    notCaseOfficerPerson = new Person(2, null, null, null, null);

    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.CASE_OFFICER)).thenReturn(Set.of(caseOfficerPerson));

    workflowAssignmentService = new WorkflowAssignmentService(camundaWorkflowService,
        pwaTeamService, consulteeGroupTeamService, consultationRequestService, teamManagementService, assignmentService);

    pwaApplicationSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    consultationSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION_CONSULTATION);

    consultationRequest = new ConsultationRequest();
    consulteeGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("TEST", "T");
    consultationRequest.setConsulteeGroup(consulteeGroupDetail.getConsulteeGroup());

    when(consultationRequestService.getConsultationRequestByIdOrThrow(eq(consultationSubject.getBusinessKey()))).thenReturn(
        consultationRequest);

  }

  @Test
  public void getAssignmentCandidates_caseOfficer() {

    assertThat(workflowAssignmentService.getAssignmentCandidates(pwaApplicationSubject,
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)).containsOnly(caseOfficerPerson);

  }

  @Test
  public void getAssignmentCandidates_consultationResponder_respondersExist() {

    var person1 = new Person(1, null, null, null, null);
    var person2 = new Person(2, null, null, null, null);

    when(consulteeGroupTeamService.getTeamMembersForGroup(eq(consulteeGroupDetail.getConsulteeGroup()))).thenReturn(
        List.of(
            new ConsulteeGroupTeamMember(consulteeGroupDetail.getConsulteeGroup(), person1, Set.of(
                ConsulteeGroupMemberRole.RESPONDER, ConsulteeGroupMemberRole.RECIPIENT)),
            new ConsulteeGroupTeamMember(consulteeGroupDetail.getConsulteeGroup(), person2,
                Set.of(ConsulteeGroupMemberRole.RECIPIENT))
        ));

    var responderPeople = workflowAssignmentService.getAssignmentCandidates(consultationSubject,
        PwaApplicationConsultationWorkflowTask.RESPONSE);

    assertThat(responderPeople).containsOnly(person1);

  }

  @Test
  public void getAssignmentCandidates_consultationResponder_noResponders() {

    when(consulteeGroupTeamService.getTeamMembersForGroup(eq(consulteeGroupDetail.getConsulteeGroup()))).thenReturn(
        List.of(
            new ConsulteeGroupTeamMember(consulteeGroupDetail.getConsulteeGroup(), new Person(),
                Set.of(ConsulteeGroupMemberRole.RECIPIENT)),
            new ConsulteeGroupTeamMember(consulteeGroupDetail.getConsulteeGroup(), new Person(),
                Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER))
        ));

    assertThat(workflowAssignmentService.getAssignmentCandidates(consultationSubject,
        PwaApplicationConsultationWorkflowTask.RESPONSE)).isEmpty();

  }

  @Test(expected = IllegalArgumentException.class)
  public void getAssignmentCandidates_consultationResponder_invalidWorkflow() {

    workflowAssignmentService.getAssignmentCandidates(pwaApplicationSubject,
        PwaApplicationConsultationWorkflowTask.RESPONSE);

  }

  @Test
  public void getAssignmentCandidates_noAssignment() {

    assertThat(workflowAssignmentService.getAssignmentCandidates(pwaApplicationSubject,
        PwaApplicationWorkflowTask.PREPARE_APPLICATION)).isEmpty();

  }

  @Test
  public void assign_success() {

    var app = new PwaApplication();

    workflowAssignmentService.assign(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson, notCaseOfficerPerson);

    verify(camundaWorkflowService, times(1)).assignTaskToUser(
        new WorkflowTaskInstance(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW),
        caseOfficerPerson);

    verify(assignmentService, times(1))
        .createOrUpdateAssignment(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, caseOfficerPerson, notCaseOfficerPerson);

  }

  @Test(expected = WorkflowAssignmentException.class)
  public void assign_invalid() {

    var app = new PwaApplication();

    workflowAssignmentService.assign(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, notCaseOfficerPerson,
        caseOfficerPerson);

  }

  @Test
  public void assignTaskNoException_success() {

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
  public void assignTaskNoException_invalidAssigneePerson() {

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
  public void triggerWorkflowMessageAndAssertTaskExists_whenExpectedTaskExists() {
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

  @Test(expected = WorkflowAssignmentException.class)
  public void triggerWorkflowMessageAndAssertTaskExists_whenExpectedTaskDoesNotExists() {
    var testMessageName = "TEST";

    var mockWorkFlowTask = mock(UserWorkflowTask.class);
    when(camundaWorkflowService.getAllActiveWorkflowTasks(pwaApplicationSubject)).thenReturn(Set.of());

    var genericMessageEvent = GenericMessageEvent.from(pwaApplicationSubject, testMessageName);
    workflowAssignmentService.triggerWorkflowMessageAndAssertTaskExists(genericMessageEvent, mockWorkFlowTask);

  }

  @Test
  public void getAssignee_assigneeExists_personExists_fullOptional() {

    var person = new Person(1, null, null, null, null);

    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.of(new PersonId(1)));
    when(teamManagementService.getPerson(1)).thenReturn(person);

    var taskInstance = new WorkflowTaskInstance(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);

    var retrievedPerson = workflowAssignmentService.getAssignee(taskInstance);

    assertThat(retrievedPerson).contains(person);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getAssignee_assigneeExists_personDoesntExist_error() {

    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.of(new PersonId(1)));
    when(teamManagementService.getPerson(1)).thenThrow(PwaEntityNotFoundException.class);

    var taskInstance = new WorkflowTaskInstance(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);

    workflowAssignmentService.getAssignee(taskInstance);

  }

  @Test
  public void getAssignee_assigneeDoesntExist_emptyOptional() {


    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.empty());

    var taskInstance = new WorkflowTaskInstance(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);

    var retrievedPerson = workflowAssignmentService.getAssignee(taskInstance);

    assertThat(retrievedPerson).isEmpty();

  }

  @Test
  public void clearAssignments_verifyServiceInteractions() {

    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    workflowAssignmentService.clearAssignments(workflowSubject);

    verify(assignmentService, times(1)).clearAssignments(workflowSubject);

  }

}
