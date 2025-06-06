package uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.UserWorkflowTask;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowMessageEvent;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowSubject;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.teams.Role;

@Service
public class WorkflowAssignmentService {

  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaTeamService pwaTeamService;
  private final ConsultationRequestService consultationRequestService;
  private final AssignmentService assignmentService;
  private final PersonService personService;

  @Autowired
  public WorkflowAssignmentService(CamundaWorkflowService camundaWorkflowService,
                                   PwaTeamService pwaTeamService,
                                   ConsultationRequestService consultationRequestService,
                                   AssignmentService assignmentService, PersonService personService) {
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaTeamService = pwaTeamService;
    this.consultationRequestService = consultationRequestService;
    this.assignmentService = assignmentService;
    this.personService = personService;
  }

  /**
   * Return a set of persons who can be assigned to the passed-in task as they have the correct roles etc.
   */
  public Set<Person> getAssignmentCandidates(WorkflowSubject workflowSubject, UserWorkflowTask task) {

    if (task.getAssignment() == null) {
      return Set.of();
    }

    switch (task.getAssignment()) {

      case CASE_OFFICER:
        return pwaTeamService.getPeopleWithRegulatorRole(Role.CASE_OFFICER);

      case CONSULTATION_RESPONDER:

        if (workflowSubject.getWorkflowType() != WorkflowType.PWA_APPLICATION_CONSULTATION) {
          throw new IllegalArgumentException(String.format(
              "CONSULTATION_RESPONDER not valid assignment for workflow: %s", workflowSubject.getWorkflowType()));
        }

        // get consultee group from request, then return responders in that group
        var consulteeGroup = consultationRequestService.getConsultationRequestByIdOrThrow(workflowSubject.getBusinessKey())
            .getConsulteeGroup();

        return pwaTeamService.getPeopleByConsulteeGroupAndRoleIn(consulteeGroup, Set.of(Role.RESPONDER));

      default:
        return Set.of();

    }

  }

  /**
   * Use this method to assign a task without throwing an exception if there's a problem.
   */
  @Transactional
  public AssignTaskResult assignTaskNoException(WorkflowSubject workflowSubject,
                                                UserWorkflowTask task,
                                                Person personToAssign,
                                                Person assigningPerson) {
    return assignTaskInternal(
        workflowSubject,
        task,
        personToAssign,
        assigningPerson
    );
  }

  /**
   * Use this method to assign a task if you want to return exceptions up the call stack if there's a problem.
   */
  @Transactional
  public void assign(WorkflowSubject workflowSubject,
                     UserWorkflowTask task,
                     Person personToAssign,
                     Person assigningPerson) {
    var assignResult = assignTaskInternal(
        workflowSubject,
        task,
        personToAssign,
        assigningPerson
    );

    if (assignResult.equals(AssignTaskResult.ASSIGNMENT_CANDIDATE_INVALID)) {
      throw new WorkflowAssignmentException(String.format(
          "Can't assign person with ID: %s to task [%s] for %s with ID: %s as they are not a valid assignment candidate.",
          personToAssign.getId().asInt(),
          task.getTaskName(),
          workflowSubject.getClass().getName(),
          workflowSubject.getBusinessKey()));
    }


  }

  private AssignTaskResult assignTaskInternal(WorkflowSubject workflowSubject,
                                              UserWorkflowTask task,
                                              Person personToAssign,
                                              Person assigningPerson) {
    boolean allowed = getAssignmentCandidates(workflowSubject, task).stream()
        .anyMatch(person -> person.getId().equals(personToAssign.getId()));

    if (!allowed) {
      return AssignTaskResult.ASSIGNMENT_CANDIDATE_INVALID;
    }

    camundaWorkflowService.assignTaskToUser(new WorkflowTaskInstance(workflowSubject, task), personToAssign);
    assignmentService.createOrUpdateAssignment(workflowSubject, task, personToAssign, assigningPerson);
    return AssignTaskResult.SUCCESS;
  }

  @Transactional
  public void triggerWorkflowMessageAndAssertTaskExists(WorkflowMessageEvent messageEvent, UserWorkflowTask userWorkflowTask) {

    camundaWorkflowService.triggerMessageEvent(
        messageEvent.getWorkflowSubject(), messageEvent.getEventName());

    var searchTaskExists = camundaWorkflowService.getAllActiveWorkflowTasks(messageEvent.getWorkflowSubject())
        .stream()
        .anyMatch(workflowTaskInstance -> userWorkflowTask.getTaskKey().equals(workflowTaskInstance.getTaskKey()));

    if (!searchTaskExists) {
      throw new WorkflowAssignmentException(
          "Expected task to exist after message event but didnt not. \n" + messageEvent.toString() +
              "\n taskName: " + userWorkflowTask.getTaskName()
      );
    }
  }

  public Optional<Person> getAssignee(WorkflowTaskInstance workflowTaskInstance) {
    return camundaWorkflowService
        .getAssignedPersonId(workflowTaskInstance)
        .map(personService::getPersonById);
  }

  public void clearAssignments(WorkflowSubject workflowSubject) {
    assignmentService.clearAssignments(workflowSubject);
  }

  public enum AssignTaskResult {
    SUCCESS,
    ASSIGNMENT_CANDIDATE_INVALID
  }

}
