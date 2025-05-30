package uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.integrations.camunda.external.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

  @Mock
  private AssignmentRepository assignmentRepository;

  @Mock
  private AssignmentAuditService assignmentAuditService;

  private AssignmentService assignmentService;

  @Captor
  private ArgumentCaptor<Assignment> assignmentCaptor;

  private final Person assignee = PersonTestUtil.createPersonFrom(new PersonId(1));
  private final Person assigner = PersonTestUtil.createPersonFrom(new PersonId(2));

  @BeforeEach
  void setUp() throws Exception {

    assignmentService = new AssignmentService(assignmentRepository, assignmentAuditService);

  }

  @Test
  void createOrUpdateAssignment_newAssignment() {

    var subject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    var task = PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW;

    assignmentService.createOrUpdateAssignment(subject, task, assignee, assigner);

    verify(assignmentRepository, times(1)).save(assignmentCaptor.capture());
    verify(assignmentAuditService, times(1)).auditAssignment(assignmentCaptor.getValue(), task, assigner);

    assertThat(assignmentCaptor.getValue()).satisfies(assignment -> {
      assertThat(assignment.getBusinessKey()).isEqualTo(subject.getBusinessKey());
      assertThat(assignment.getWorkflowType()).isEqualTo(subject.getWorkflowType());
      assertThat(assignment.getWorkflowAssignment()).isEqualTo(task.getAssignment());
      assertThat(assignment.getAssigneePersonId()).isEqualTo(assignee.getId());
    });

  }

  @Test
  void createOrUpdateAssignment_updatedAssignment() {

    var subject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    var task = PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW;

    var assignment = new Assignment();
    assignment.setId(1);
    assignment.setBusinessKey(subject.getBusinessKey());
    assignment.setWorkflowType(subject.getWorkflowType());
    assignment.setWorkflowAssignment(task.getAssignment());
    assignment.setAssigneePersonId(assignee.getId());

    var newAssignee = PersonTestUtil.createPersonFrom(new PersonId(5));

    when(assignmentRepository.findByBusinessKeyAndWorkflowTypeAndWorkflowAssignment(
        subject.getBusinessKey(),
        subject.getWorkflowType(),
        task.getAssignment()
        )).thenReturn(Optional.of(assignment));

    assignmentService.createOrUpdateAssignment(subject, task, newAssignee, assigner);

    verify(assignmentRepository, times(1)).save(assignmentCaptor.capture());
    verify(assignmentAuditService, times(1)).auditAssignment(assignmentCaptor.getValue(), task, assigner);

    assertThat(assignmentCaptor.getValue()).satisfies(savedAssignment -> {
      assertThat(savedAssignment.getId()).isEqualTo(assignment.getId());
      assertThat(savedAssignment.getBusinessKey()).isEqualTo(assignment.getBusinessKey());
      assertThat(savedAssignment.getWorkflowType()).isEqualTo(assignment.getWorkflowType());
      assertThat(savedAssignment.getWorkflowAssignment()).isEqualTo(assignment.getWorkflowAssignment());
      assertThat(savedAssignment.getAssigneePersonId()).isEqualTo(newAssignee.getId());
    });

  }

  @Test
  void getAssignmentsForPerson() {

    var person = PersonTestUtil.createDefaultPerson();

    var appAssignment = new Assignment(1, WorkflowType.PWA_APPLICATION, WorkflowAssignment.CASE_OFFICER, person.getId());
    var consultationAssignment = new Assignment(1, WorkflowType.PWA_APPLICATION_CONSULTATION, WorkflowAssignment.CONSULTATION_RESPONDER, person.getId());

    when(assignmentRepository.findByAssigneePersonId(person.getId()))
        .thenReturn(List.of(appAssignment, consultationAssignment));

    var map = assignmentService.getAssignmentsForPerson(person);

    assertThat(map).containsExactlyInAnyOrderEntriesOf(Map.of(
        WorkflowType.PWA_APPLICATION, List.of(appAssignment),
        WorkflowType.PWA_APPLICATION_CONSULTATION, List.of(consultationAssignment)
    ));

  }

  @Test
  void getAssignmentsForPerson_workflowSubject() {

    var person = PersonTestUtil.createDefaultPerson();

    var appAssignment = new Assignment(1, WorkflowType.PWA_APPLICATION, WorkflowAssignment.CASE_OFFICER, person.getId());

    when(assignmentRepository.findByBusinessKeyAndWorkflowTypeAndAssigneePersonId(1, WorkflowType.PWA_APPLICATION, person.getId()))
        .thenReturn(List.of(appAssignment));

    var list = assignmentService.getAssignmentsForPerson(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION), person);

    assertThat(list).containsExactly(appAssignment);

  }

  @Test
  void clearAssignments() {

    var assignment = new Assignment(1, WorkflowType.PWA_APPLICATION, WorkflowAssignment.CASE_OFFICER, new PersonId(1));
    when(assignmentRepository.findByBusinessKeyAndWorkflowType(1, WorkflowType.PWA_APPLICATION))
        .thenReturn(List.of(assignment));

    assignmentService.clearAssignments(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION));

    verify(assignmentRepository, times(1)).deleteAll(List.of(assignment));

  }

  @Test
  void getAssignments() {

    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    assignmentService.getAssignments(workflowSubject);

    verify(assignmentRepository, times(1))
        .findByBusinessKeyAndWorkflowType(workflowSubject.getBusinessKey(), workflowSubject.getWorkflowType());

  }

  @Test
  void getAssignmentsForWorkflowAssignment() {

    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    assignmentService.getAssignmentsForWorkflowAssignment(workflowSubject, WorkflowAssignment.CASE_OFFICER);

    verify(assignmentRepository).findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(workflowSubject.getBusinessKey(),
        WorkflowAssignment.CASE_OFFICER, WorkflowType.PWA_APPLICATION);

  }

  @Test
  void getAssignmentOrError_verifyRepoInteraction_noException() {

    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    var assignment = new Assignment(
        workflowSubject.getBusinessKey(), workflowSubject.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, new PersonId(1));

    when(assignmentRepository.findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(
        workflowSubject.getBusinessKey(), WorkflowAssignment.CASE_OFFICER, workflowSubject.getWorkflowType()))
        .thenReturn(Optional.of(assignment));

    assignmentService.getAssignmentOrError(workflowSubject, WorkflowAssignment.CASE_OFFICER);
    verify(assignmentRepository).findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(workflowSubject.getBusinessKey(),
        WorkflowAssignment.CASE_OFFICER, WorkflowType.PWA_APPLICATION);
  }

  @Test
  void getAssignmentOrError_verifyRepoInteraction_noAssignmentFound_exceptionThrown() {
    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    when(assignmentRepository.findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(
          workflowSubject.getBusinessKey(), WorkflowAssignment.CASE_OFFICER, workflowSubject.getWorkflowType()))
          .thenReturn(Optional.empty());
    assertThrows(IllegalStateException.class, () ->
      assignmentService.getAssignmentOrError(workflowSubject, WorkflowAssignment.CASE_OFFICER));
  }

}