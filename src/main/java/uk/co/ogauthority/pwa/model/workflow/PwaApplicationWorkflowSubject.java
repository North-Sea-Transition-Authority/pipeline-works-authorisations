package uk.co.ogauthority.pwa.model.workflow;

import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;

/**
 * Implementation to capture relevant information without requiring access to a PWA application entity object.
 */
public class PwaApplicationWorkflowSubject implements WorkflowSubject {

  private final Integer businessKey;
  private final WorkflowType workflowType;

  public PwaApplicationWorkflowSubject(Integer businessKey,
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
