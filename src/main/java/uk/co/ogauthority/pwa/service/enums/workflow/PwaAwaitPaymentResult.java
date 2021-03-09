package uk.co.ogauthority.pwa.service.enums.workflow;

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
