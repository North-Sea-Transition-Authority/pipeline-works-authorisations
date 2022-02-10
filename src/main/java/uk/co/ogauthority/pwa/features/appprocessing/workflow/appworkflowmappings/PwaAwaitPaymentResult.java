package uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings;

import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowProperty;

/**
 * Defines workflow property values associated with an initial review payment decision.
 */
public enum PwaAwaitPaymentResult implements WorkflowProperty {

  PAID("awaitApplicationPaymentResult", "PAID"),
  CANCELLED("awaitApplicationPaymentResult", "CANCELLED");

  private final String propertyName;

  private final String propertyValue;

  PwaAwaitPaymentResult(String propertyName, String propertyValue) {
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
  }

  @Override
  public String getPropertyName() {
    return this.propertyName;
  }

  @Override
  public String getPropertyValue() {
    return this.propertyValue;
  }
}
