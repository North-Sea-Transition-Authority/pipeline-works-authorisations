package uk.co.ogauthority.pwa.integrations.camunda.external;

import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;

/**
 * Interface to enforce common functionality but allow separation between workflow tasks for different workflow types.
 */
public interface UserWorkflowTask {

  /**
   * Task key is the name assigned in the .bpmn document for the stage.
   */
  String getTaskKey();

  /**
   * Determines which type of user can be assigned the task.
   */
  WorkflowAssignment getAssignment();

  /**
   * Return the name of the enum value.
   */
  String getTaskName();

}
