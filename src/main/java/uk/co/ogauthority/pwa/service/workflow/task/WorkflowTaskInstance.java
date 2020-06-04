package uk.co.ogauthority.pwa.service.workflow.task;

import java.util.Objects;
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;

/**
 * Data class to store a workflow subject object (e.g. PWA application) and a task that is part of that object's workflow.
 */
public class WorkflowTaskInstance {

  private final WorkflowSubject workflowSubject;

  private final UserWorkflowTask userWorkflowTask;

  /**
   * Verify that the task param is part of the workflow subject's workflow before creating the object.
   * @throws WorkflowException if task param isn't part of the subject's workflow
   */
  public WorkflowTaskInstance(WorkflowSubject workflowSubject,
                              UserWorkflowTask userWorkflowTask) {

    if (!userWorkflowTask.getWorkflowType().equals(workflowSubject.getWorkflowType())) {
      throw new WorkflowException(String.format(
          "Can't get task [%s] for %s with ID %s as workflow types don't match: task workflow [%s], subject workflow [%s]",
          userWorkflowTask.getTaskName(),
          workflowSubject.getClass().getName(),
          workflowSubject.getBusinessKey(),
          userWorkflowTask.getWorkflowType(),
          workflowSubject.getWorkflowType()));
    }

    this.workflowSubject = workflowSubject;
    this.userWorkflowTask = userWorkflowTask;

  }

  public Integer getBusinessKey() {
    return workflowSubject.getBusinessKey();
  }

  public WorkflowType getWorkflowType() {
    return userWorkflowTask.getWorkflowType();
  }

  public String getTaskKey() {
    return userWorkflowTask.getTaskKey();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowTaskInstance that = (WorkflowTaskInstance) o;
    return Objects.equals(workflowSubject, that.workflowSubject)
        && Objects.equals(userWorkflowTask, that.userWorkflowTask);
  }

  @Override
  public int hashCode() {
    return Objects.hash(workflowSubject, userWorkflowTask);
  }
}
