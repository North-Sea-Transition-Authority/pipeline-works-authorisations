package uk.co.ogauthority.pwa.service.workflow.assignment;

import java.time.Clock;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.AssignmentAudit;
import uk.co.ogauthority.pwa.repository.workflow.assignment.AssignmentAuditRepository;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;

@Service
public class AssignmentAuditService {

  private final Clock clock;
  private final AssignmentAuditRepository assignmentAuditRepository;

  @Autowired
  public AssignmentAuditService(@Qualifier("utcClock") Clock clock,
                                AssignmentAuditRepository assignmentAuditRepository) {
    this.clock = clock;
    this.assignmentAuditRepository = assignmentAuditRepository;
  }

  @Transactional
  public void auditAssignment(WorkflowSubject workflowSubject,
                              UserWorkflowTask task,
                              Person assignee,
                              Person assigningPerson) {

    var audit = new AssignmentAudit();
    audit.setWorkflowType(workflowSubject.getWorkflowType());
    audit.setBusinessKey(workflowSubject.getBusinessKey());
    audit.setAssignment(task.getAssignment());
    audit.setTaskKey(task.getTaskKey());
    audit.setAssigneePersonId(assignee.getId().asInt());
    audit.setAssignedByPersonId(assigningPerson.getId().asInt());
    audit.setAssignmentTimestamp(clock.instant());

    assignmentAuditRepository.save(audit);

  }

}
