package uk.co.ogauthority.pwa.service.workflow.assignment;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowMessageEvent;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@Service
public class WorkflowAssignmentService {

  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaTeamService pwaTeamService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final ConsultationRequestService consultationRequestService;
  private final TeamManagementService teamManagementService;
  private final AssignmentService assignmentService;

  @Autowired
  public WorkflowAssignmentService(CamundaWorkflowService camundaWorkflowService,
                                   PwaTeamService pwaTeamService,
                                   ConsulteeGroupTeamService consulteeGroupTeamService,
                                   ConsultationRequestService consultationRequestService,
                                   TeamManagementService teamManagementService,
                                   AssignmentService assignmentService) {
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaTeamService = pwaTeamService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.consultationRequestService = consultationRequestService;
    this.teamManagementService = teamManagementService;
    this.assignmentService = assignmentService;
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
        return pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.CASE_OFFICER);

      case CONSULTATION_RESPONDER:

        if (workflowSubject.getWorkflowType() != WorkflowType.PWA_APPLICATION_CONSULTATION) {
          throw new IllegalArgumentException(String.format(
              "CONSULTATION_RESPONDER not valid assignment for workflow: %s", workflowSubject.getWorkflowType()));
        }

        // get consultee group from request, then return responders in that group
        var consulteeGroup = consultationRequestService.getConsultationRequestByIdOrThrow(workflowSubject.getBusinessKey())
            .getConsulteeGroup();

        return consulteeGroupTeamService.getTeamMembersForGroup(consulteeGroup).stream()
            .filter(member -> member.getRoles().contains(ConsulteeGroupMemberRole.RESPONDER))
            .map(ConsulteeGroupTeamMember::getPerson)
            .collect(Collectors.toSet());

      default:
        return Set.of();

    }

  }

  @Transactional
  /**
   * Use this method to assign a task without throwing an exception if theres a problem.
   */
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
        .map(personId -> teamManagementService.getPerson(personId.asInt()));

  }

  public void clearAssignments(WorkflowSubject workflowSubject) {
    assignmentService.clearAssignments(workflowSubject);
  }


  public enum AssignTaskResult {
    SUCCESS,
    ASSIGNMENT_CANDIDATE_INVALID
  }
}
