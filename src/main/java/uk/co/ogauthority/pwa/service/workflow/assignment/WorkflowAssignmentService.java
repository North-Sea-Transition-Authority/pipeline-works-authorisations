package uk.co.ogauthority.pwa.service.workflow.assignment;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowMessageEvent;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@Service
public class WorkflowAssignmentService {

  private final CamundaWorkflowService camundaWorkflowService;
  private final AssignmentAuditService assignmentAuditService;
  private final TeamService teamService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final ConsultationRequestService consultationRequestService;
  private final TeamManagementService teamManagementService;

  public WorkflowAssignmentService(CamundaWorkflowService camundaWorkflowService,
                                   AssignmentAuditService assignmentAuditService,
                                   TeamService teamService,
                                   ConsulteeGroupTeamService consulteeGroupTeamService,
                                   ConsultationRequestService consultationRequestService,
                                   TeamManagementService teamManagementService) {
    this.camundaWorkflowService = camundaWorkflowService;
    this.assignmentAuditService = assignmentAuditService;
    this.teamService = teamService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.consultationRequestService = consultationRequestService;
    this.teamManagementService = teamManagementService;
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
        return teamService.getTeamMembers(teamService.getRegulatorTeam()).stream()
            .filter(member -> member.getRoleSet().stream()
                .map(PwaRole::getName)
                .anyMatch(roleName -> roleName.equals(PwaRegulatorRole.CASE_OFFICER.getPortalTeamRoleName())))
            .map(PwaTeamMember::getPerson)
            .collect(Collectors.toSet());

      case CONSULTATION_RESPONDER:

        if (workflowSubject.getWorkflowType() != WorkflowType.PWA_APPLICATION_CONSULTATION) {
          throw new IllegalArgumentException(String.format(
              "CONSULTATION_RESPONDER not valid assignment for workflow: %s", workflowSubject.getWorkflowType()));
        }

        // get consultee group from request, then return responders in that group
        var consulteeGroup = consultationRequestService.getConsultationRequestById(workflowSubject.getBusinessKey())
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
  public void assign(WorkflowSubject workflowSubject,
                     UserWorkflowTask task,
                     Person personToAssign,
                     Person assigningPerson) {

    boolean allowed = getAssignmentCandidates(workflowSubject, task).stream()
        .anyMatch(person -> person.getId().equals(personToAssign.getId()));

    if (!allowed) {
      throw new WorkflowAssignmentException(String.format(
          "Can't assign person with ID: %s to task [%s] for %s with ID: %s as they are not a valid assignment candidate.",
          personToAssign.getId().asInt(),
          task.getTaskName(),
          workflowSubject.getClass().getName(),
          workflowSubject.getBusinessKey()));
    }

    camundaWorkflowService.assignTaskToUser(new WorkflowTaskInstance(workflowSubject, task), personToAssign);
    assignmentAuditService.auditAssignment(workflowSubject, task, personToAssign, assigningPerson);

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

}
