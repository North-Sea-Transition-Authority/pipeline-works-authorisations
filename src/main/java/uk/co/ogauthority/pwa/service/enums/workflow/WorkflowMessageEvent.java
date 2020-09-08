package uk.co.ogauthority.pwa.service.enums.workflow;

/**
 * Workflow transitions/communication between tasks can be triggered from arbitrary tasks in the workflow.
 */
public interface WorkflowMessageEvent {

  WorkflowSubject getWorkflowSubject();

  String getEventName();
}
