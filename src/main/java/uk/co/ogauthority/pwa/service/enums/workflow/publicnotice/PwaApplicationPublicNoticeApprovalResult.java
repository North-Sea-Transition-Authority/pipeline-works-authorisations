package uk.co.ogauthority.pwa.service.enums.workflow.publicnotice;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowProperty;

/**
 * Defines workflow property values associated with a manager approval decision.
 */
public enum PwaApplicationPublicNoticeApprovalResult implements WorkflowProperty {

  REQUEST_APPROVED("approvalDecision", "APPROVED", "Approve"),
  REQUEST_REJECTED("approvalDecision", "REJECTED", "Reject");

  private final String propertyName;

  private final String propertyValue;

  private final String displayValue;

  PwaApplicationPublicNoticeApprovalResult(String propertyName, String propertyValue, String displayValue) {
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
    this.displayValue = displayValue;
  }

  @Override
  public String getPropertyName() {
    return this.propertyName;
  }

  @Override
  public String getPropertyValue() {
    return this.propertyValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }

  public static List<PwaApplicationPublicNoticeApprovalResult> asList() {
    return Arrays.asList(PwaApplicationPublicNoticeApprovalResult.values());
  }
}
