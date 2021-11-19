package uk.co.ogauthority.pwa.integrations.camunda.external;

/**
 * Workflow transitions/communication between tasks can be triggered from arbitrary tasks in the workflow.
 */
public interface WorkflowMessageEvent {

  WorkflowSubject getWorkflowSubject();

  String getEventName();
}
