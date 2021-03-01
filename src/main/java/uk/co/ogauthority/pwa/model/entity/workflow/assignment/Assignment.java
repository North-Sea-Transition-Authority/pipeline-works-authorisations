package uk.co.ogauthority.pwa.model.entity.workflow.assignment;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;

/**
 * Entity to store workflow assignment information for in-progress workflows.
 * These entities should be cleared when a workflow is completed.
 * An audit trail of these entities can be found in {@link AssignmentAudit}.
 */
@Entity
@Table(name = "assignments")
public class Assignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer businessKey;

  @Enumerated(EnumType.STRING)
  private WorkflowType workflowType;

  @Enumerated(EnumType.STRING)
  @Column(name = "assignment")
  private WorkflowAssignment workflowAssignment;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  private PersonId assigneePersonId;

  public Assignment() {
  }

  public Assignment(Integer businessKey,
                    WorkflowType workflowType,
                    WorkflowAssignment workflowAssignment,
                    PersonId assigneePersonId) {
    this.businessKey = businessKey;
    this.workflowType = workflowType;
    this.workflowAssignment = workflowAssignment;
    this.assigneePersonId = assigneePersonId;
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

  public WorkflowAssignment getWorkflowAssignment() {
    return workflowAssignment;
  }

  public void setWorkflowAssignment(
      WorkflowAssignment workflowAssignment) {
    this.workflowAssignment = workflowAssignment;
  }

  public PersonId getAssigneePersonId() {
    return assigneePersonId;
  }

  public void setAssigneePersonId(PersonId assigneePersonId) {
    this.assigneePersonId = assigneePersonId;
  }
}
