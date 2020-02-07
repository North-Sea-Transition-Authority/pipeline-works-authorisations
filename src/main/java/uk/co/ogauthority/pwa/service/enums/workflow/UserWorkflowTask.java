package uk.co.ogauthority.pwa.service.enums.workflow;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

/**
 * Values are defined for each stage in a Camunda workflow.
 * workflowType = the workflow the stage belongs to
 * taskName = the name assigned in the .bpmn document for the stage
 * userTypes = types of user that should be able to take the actions available at the stage
 */
public enum UserWorkflowTask {

  PREPARE_APPLICATION(WorkflowType.PWA_APPLICATION, "prepareApplication", List.of(UserType.INDUSTRY)),
  APPLICATION_REVIEW(WorkflowType.PWA_APPLICATION, "applicationReview", List.of(UserType.OGA));

  private final WorkflowType workflowType;
  private final String taskKey;
  private final List<UserType> userTypes;

  UserWorkflowTask(WorkflowType workflowType, String taskKey,
                   List<UserType> userTypes) {
    this.workflowType = workflowType;
    this.taskKey = taskKey;
    this.userTypes = userTypes;
  }

  public WorkflowType getWorkflowType() {
    return workflowType;
  }

  public String getTaskKey() {
    return taskKey;
  }

  public List<UserType> getUserTypes() {
    return userTypes;
  }

  public static UserWorkflowTask getByTaskName(String taskName) {
    return Arrays.stream(UserWorkflowTask.values())
        .filter(e -> e.getTaskKey().equals(taskName))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(String.format("Task name %s not found", taskName)));
  }

}
