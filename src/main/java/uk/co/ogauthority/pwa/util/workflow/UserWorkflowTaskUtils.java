package uk.co.ogauthority.pwa.util.workflow;

import java.lang.reflect.InvocationTargetException;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;

public class UserWorkflowTaskUtils {

  private UserWorkflowTaskUtils() {
    throw new AssertionError();
  }

  /**
   * To use this, your WorkflowType's task class must specify a static method "getByTaskKey" that retrieves the appropriate enum value
   * by filtering based on task key.
   * @param workflowType that the task is expected to belong to
   * @param taskKey of the task we're trying to retrieve
   * @return a resolved UserWorkflowTask implementation if no issues, relevant exception otherwise
   */
  public static UserWorkflowTask getTaskByWorkflowAndTaskKey(WorkflowType workflowType, String taskKey) {

    try {

      var task = workflowType.getWorkflowTaskClass().getMethod("getByTaskKey", String.class).invoke(workflowType, taskKey);

      return workflowType.getWorkflowTaskClass().cast(task);

    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(
          String.format("Couldn't access getByTaskKey method on class %s", workflowType.getWorkflowTaskClass().getName()),
          e);
    }

  }

}
