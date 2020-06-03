package uk.co.ogauthority.pwa.service.enums.workflow;

/**
 * Interface to make workflow interactions easier by getting the entity that is related to the workflow to provide
 * relevant information.
 */
public interface WorkflowSubject {

  Integer getBusinessKey();

  WorkflowType getWorkflowType();

}
