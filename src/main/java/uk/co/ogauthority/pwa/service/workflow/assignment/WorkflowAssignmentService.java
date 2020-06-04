package uk.co.ogauthority.pwa.service.workflow.assignment;

import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@Service
public class WorkflowAssignmentService {

  private final CamundaWorkflowService camundaWorkflowService;
  private final AssignmentAuditService assignmentAuditService;
  private final TeamService teamService;

  public WorkflowAssignmentService(CamundaWorkflowService camundaWorkflowService,
                                   AssignmentAuditService assignmentAuditService,
                                   TeamService teamService) {
    this.camundaWorkflowService = camundaWorkflowService;
    this.assignmentAuditService = assignmentAuditService;
    this.teamService = teamService;
  }

  /**
   * Return a set of persons who can be assigned to the passed-in task as they have the correct roles etc.
   */
  public Set<Person> getAssignmentCandidates(UserWorkflowTask task) {

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

      default:
        return Set.of();

    }

  }

  @Transactional
  public void assign(WorkflowSubject workflowSubject,
                     UserWorkflowTask task,
                     Person personToAssign,
                     Person assigningPerson) {

    boolean allowed = getAssignmentCandidates(task).stream()
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

}
