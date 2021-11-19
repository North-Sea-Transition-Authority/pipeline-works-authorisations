package uk.co.ogauthority.pwa.integrations.camunda.external;

/**
 * Implementation to capture relevant information without requiring access to an entity object.
 */
public class GenericWorkflowSubject implements WorkflowSubject {

  private final Integer businessKey;
  private final WorkflowType workflowType;

  public GenericWorkflowSubject(Integer businessKey,
                                WorkflowType workflowType) {
    this.businessKey = businessKey;
    this.workflowType = workflowType;
  }

  @Override
  public Integer getBusinessKey() {
    return businessKey;
  }

  @Override
  public WorkflowType getWorkflowType() {
    return workflowType;
  }
}
