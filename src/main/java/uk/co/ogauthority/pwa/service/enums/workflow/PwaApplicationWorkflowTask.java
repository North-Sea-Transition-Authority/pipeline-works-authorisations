package uk.co.ogauthority.pwa.service.enums.workflow;

import java.util.Arrays;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;

public enum PwaApplicationWorkflowTask implements UserWorkflowTask {

  PREPARE_APPLICATION("prepareApplication", null),
  APPLICATION_REVIEW("applicationReview", null),
  CASE_OFFICER_REVIEW("caseOfficerReview", WorkflowAssignment.CASE_OFFICER);

  private final String taskKey;
  private final WorkflowAssignment workflowAssignment;

  PwaApplicationWorkflowTask(String taskKey,
                             WorkflowAssignment workflowAssignment) {
    this.taskKey = taskKey;
    this.workflowAssignment = workflowAssignment;
  }

  @Override
  public WorkflowType getWorkflowType() {
    return WorkflowType.PWA_APPLICATION;
  }

  @Override
  public WorkflowAssignment getAssignment() {
    return workflowAssignment;
  }

  @Override
  public String getTaskName() {
    return name();
  }

  @Override
  public String getTaskKey() {
    return taskKey;
  }

  public static PwaApplicationWorkflowTask getByTaskKey(String taskKey) {
    return Arrays.stream(PwaApplicationWorkflowTask.values())
        .filter(e -> e.getTaskKey().equals(taskKey))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(String.format("Task key %s not found", taskKey)));
  }

}
