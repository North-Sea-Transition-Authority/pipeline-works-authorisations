package uk.co.ogauthority.pwa.model.entity.pwaapplications;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;


@Entity
@Table(name = "pwa_app_assignments")
@Immutable
public class PwaAppAssignmentView {

  @Id
  private Integer id;
  private Integer pwaApplicationId;
  @Enumerated(EnumType.STRING)
  private WorkflowAssignment assignment;
  private Integer assigneePersonId;
  private String assigneeName;
  private Instant assignmentTimestamp;



  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public void setPwaApplicationId(Integer pwaApplicationId) {
    this.pwaApplicationId = pwaApplicationId;
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

  public String getAssigneeName() {
    return assigneeName;
  }

  public void setAssigneeName(String assigneeName) {
    this.assigneeName = assigneeName;
  }

  public Instant getAssignmentTimestamp() {
    return assignmentTimestamp;
  }

  public void setAssignmentTimestamp(Instant assignmentTimestamp) {
    this.assignmentTimestamp = assignmentTimestamp;
  }
}
