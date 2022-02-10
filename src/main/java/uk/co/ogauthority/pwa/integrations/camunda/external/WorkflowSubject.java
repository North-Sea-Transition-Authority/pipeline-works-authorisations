package uk.co.ogauthority.pwa.integrations.camunda.external;

/**
 * Interface to make workflow interactions easier by getting the entity that is related to the workflow to provide
 * relevant information.
 */
public interface WorkflowSubject {

  Integer getBusinessKey();

  WorkflowType getWorkflowType();

  default String getDebugString() {
    return "WorkflowSubject{workflowType=" + getWorkflowType() + ", businessKey=" + getBusinessKey() + "}";
  }

}
