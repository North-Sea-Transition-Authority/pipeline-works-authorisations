package uk.co.ogauthority.pwa.service.workflow.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.model.workflow.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.repository.workflow.assignment.AssignmentRepository;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;

@RunWith(MockitoJUnitRunner.class)
public class AssignmentServiceTest {

  @Mock
  private AssignmentRepository assignmentRepository;

  @Mock
  private AssignmentAuditService assignmentAuditService;

  private AssignmentService assignmentService;

  @Captor
  private ArgumentCaptor<Assignment> assignmentCaptor;

  private final Person assignee = PersonTestUtil.createPersonFrom(new PersonId(1));
  private final Person assigner = PersonTestUtil.createPersonFrom(new PersonId(2));

  @Before
  public void setUp() throws Exception {

    assignmentService = new AssignmentService(assignmentRepository, assignmentAuditService);

  }

  @Test
  public void createOrUpdateAssignment_newAssignment() {

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
  public void createOrUpdateAssignment_updatedAssignment() {

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
  public void getAssignmentsForPerson() {

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
  public void getAssignmentsForPerson_workflowSubject() {

    var person = PersonTestUtil.createDefaultPerson();

    var appAssignment = new Assignment(1, WorkflowType.PWA_APPLICATION, WorkflowAssignment.CASE_OFFICER, person.getId());

    when(assignmentRepository.findByBusinessKeyAndWorkflowTypeAndAssigneePersonId(1, WorkflowType.PWA_APPLICATION, person.getId()))
        .thenReturn(List.of(appAssignment));

    var list = assignmentService.getAssignmentsForPerson(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION), person);

    assertThat(list).containsExactly(appAssignment);

  }

  @Test
  public void clearAssignments() {

    var assignment = new Assignment(1, WorkflowType.PWA_APPLICATION, WorkflowAssignment.CASE_OFFICER, new PersonId(1));
    when(assignmentRepository.findByBusinessKeyAndWorkflowType(1, WorkflowType.PWA_APPLICATION))
        .thenReturn(List.of(assignment));

    assignmentService.clearAssignments(new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION));

    verify(assignmentRepository, times(1)).deleteAll(List.of(assignment));

  }

  @Test
  public void getAssignments() {

    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    assignmentService.getAssignments(workflowSubject);

    verify(assignmentRepository, times(1))
        .findByBusinessKeyAndWorkflowType(workflowSubject.getBusinessKey(), workflowSubject.getWorkflowType());

  }

  @Test
  public void getAssignmentsForWorkflowAssignment() {

    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    assignmentService.getAssignmentsForWorkflowAssignment(workflowSubject, WorkflowAssignment.CASE_OFFICER);

    verify(assignmentRepository).findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(workflowSubject.getBusinessKey(),
        WorkflowAssignment.CASE_OFFICER, WorkflowType.PWA_APPLICATION);

  }

  @Test
  public void getCaseOfficerAssignment_verifyRepoInteraction_noException() {

    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    var assignment = new Assignment(
        workflowSubject.getBusinessKey(), workflowSubject.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, new PersonId(1));

    when(assignmentRepository.findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(
        workflowSubject.getBusinessKey(), WorkflowAssignment.CASE_OFFICER, workflowSubject.getWorkflowType()))
        .thenReturn(Optional.of(assignment));

    assignmentService.getCaseOfficerAssignment(workflowSubject);
    verify(assignmentRepository).findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(workflowSubject.getBusinessKey(),
        WorkflowAssignment.CASE_OFFICER, WorkflowType.PWA_APPLICATION);
  }

  @Test(expected = WorkflowAssignmentException.class)
  public void getCaseOfficerAssignment_verifyRepoInteraction_noAssignmentFound_exceptionThrown() {

    var workflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    when(assignmentRepository.findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(
        workflowSubject.getBusinessKey(), WorkflowAssignment.CASE_OFFICER, workflowSubject.getWorkflowType()))
        .thenReturn(Optional.empty());

    assignmentService.getCaseOfficerAssignment(workflowSubject);
    verify(assignmentRepository).findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(workflowSubject.getBusinessKey(),
        WorkflowAssignment.CASE_OFFICER, workflowSubject.getWorkflowType());
  }

}