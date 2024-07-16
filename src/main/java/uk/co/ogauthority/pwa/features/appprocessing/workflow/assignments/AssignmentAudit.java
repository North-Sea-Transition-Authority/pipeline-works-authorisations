package uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;

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
