package uk.co.ogauthority.pwa.model.entity.workflow.assignment;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;

/**
 * Entity to store workflow assignment information.
 */
@Entity
@Table(name = "assignment_audit_log")
public class AssignmentAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer businessKey;

  @Enumerated(EnumType.STRING)
  private WorkflowType workflowType;

  private String taskKey;

  @Enumerated(EnumType.STRING)
  private WorkflowAssignment assignment;

  private Integer assigneePersonId;

  private Integer assignedByPersonId;

  private Instant assignmentTimestamp;

  public AssignmentAudit() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(Integer businessKey) {
    this.businessKey = businessKey;
  }

  public WorkflowType getWorkflowType() {
    return workflowType;
  }

  public void setWorkflowType(WorkflowType workflowType) {
    this.workflowType = workflowType;
  }

  public String getTaskKey() {
    return taskKey;
  }

  public void setTaskKey(String taskKey) {
    this.taskKey = taskKey;
  }

  public WorkflowAssignment getAssignment() {
    return assignment;
  }

  public void setAssignment(WorkflowAssignment assignment) {
    this.assignment = assignment;
  }

  public Integer getAssigneePersonId() {
    return assigneePersonId;
  }

  public void setAssigneePersonId(Integer assigneePersonId) {
    this.assigneePersonId = assigneePersonId;
  }

  public Integer getAssignedByPersonId() {
    return assignedByPersonId;
  }

  public void setAssignedByPersonId(Integer assignedByPersonId) {
    this.assignedByPersonId = assignedByPersonId;
  }

  public Instant getAssignmentTimestamp() {
    return assignmentTimestamp;
  }

  public void setAssignmentTimestamp(Instant assignmentTimestamp) {
    this.assignmentTimestamp = assignmentTimestamp;
  }
}
