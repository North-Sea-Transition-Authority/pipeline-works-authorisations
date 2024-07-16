package uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.camunda.external.UserWorkflowTask;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowSubject;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

@Service
public class AssignmentService {

  private final AssignmentRepository assignmentRepository;
  private final AssignmentAuditService assignmentAuditService;

  @Autowired
  public AssignmentService(AssignmentRepository assignmentRepository,
                           AssignmentAuditService assignmentAuditService) {
    this.assignmentRepository = assignmentRepository;
    this.assignmentAuditService = assignmentAuditService;
  }

  @Transactional
  public void createOrUpdateAssignment(WorkflowSubject workflowSubject,
                                       UserWorkflowTask task,
                                       Person personToAssign,
                                       Person assigningPerson) {

    assignmentRepository.findByBusinessKeyAndWorkflowTypeAndWorkflowAssignment(
        workflowSubject.getBusinessKey(), workflowSubject.getWorkflowType(), task.getAssignment())
        .ifPresentOrElse(assignment -> {

          assignment.setAssigneePersonId(personToAssign.getId());
          assignmentRepository.save(assignment);

          assignmentAuditService.auditAssignment(assignment, task, assigningPerson);

        }, () -> {

              var assignment = new Assignment();
              assignment.setBusinessKey(workflowSubject.getBusinessKey());
              assignment.setWorkflowType(workflowSubject.getWorkflowType());
              assignment.setWorkflowAssignment(task.getAssignment());
              assignment.setAssigneePersonId(personToAssign.getId());

              assignmentRepository.save(assignment);

              assignmentAuditService.auditAssignment(assignment, task, assigningPerson);

          });

  }

  public Map<WorkflowType, List<Assignment>> getAssignmentsForPerson(Person person) {
    return assignmentRepository.findByAssigneePersonId(person.getId()).stream()
        .collect(Collectors.groupingBy(Assignment::getWorkflowType));
  }

  public List<Assignment> getAssignmentsForPerson(WorkflowSubject workflowSubject,
                                                  Person person) {
    return assignmentRepository.findByBusinessKeyAndWorkflowTypeAndAssigneePersonId(
        workflowSubject.getBusinessKey(), workflowSubject.getWorkflowType(), person.getId());
  }

  public void clearAssignments(WorkflowSubject workflowSubject) {

    var assignments = assignmentRepository.findByBusinessKeyAndWorkflowType(
        workflowSubject.getBusinessKey(), workflowSubject.getWorkflowType());

    assignmentRepository.deleteAll(assignments);

  }

  public List<Assignment> getAssignments(WorkflowSubject workflowSubject) {
    return assignmentRepository.findByBusinessKeyAndWorkflowType(workflowSubject.getBusinessKey(), workflowSubject.getWorkflowType());
  }

  public Optional<Assignment> getAssignmentsForWorkflowAssignment(WorkflowSubject workflowSubject, WorkflowAssignment workflowAssignment) {
    return assignmentRepository.findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(workflowSubject.getBusinessKey(), workflowAssignment,
        workflowSubject.getWorkflowType());
  }

  public Assignment getAssignmentOrError(WorkflowSubject workflowSubject, WorkflowAssignment workflowAssignment) {
    return getAssignmentsForWorkflowAssignment(workflowSubject, workflowAssignment).orElseThrow(
        () -> new IllegalStateException(String.format(
            "Cannot find assignment for workflow type: %s with business key %s and workflow assignment %s",
                workflowSubject.getWorkflowType(), workflowSubject.getBusinessKey(), workflowAssignment)));
  }

}
