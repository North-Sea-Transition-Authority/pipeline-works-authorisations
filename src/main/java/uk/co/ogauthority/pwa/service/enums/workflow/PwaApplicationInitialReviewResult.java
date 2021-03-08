package uk.co.ogauthority.pwa.service.enums.workflow;

/**
 * Defines workflow property values associated with an initial review payment decision.
 */
public enum PwaApplicationInitialReviewResult implements WorkflowProperty {

  PAYMENT_REQUIRED("chargeDecision", "REQUIRED"),
  PAYMENT_WAIVED("chargeDecision", "WAIVED");

  private final String propertyName;

  private final String propertyValue;

  PwaApplicationInitialReviewResult(String propertyName, String propertyValue) {
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
