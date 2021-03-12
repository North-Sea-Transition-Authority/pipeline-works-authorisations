package uk.co.ogauthority.pwa.service.enums.workflow.application;

import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowProperty;

/**
 * Defines workflow property values associated with a consent review decision.
 */
public enum ConsentReviewDecision implements WorkflowProperty {

  APPROVE("consentReviewDecision", "APPROVE"),
  RETURN("consentReviewDecision", "RETURN");

  private final String propertyName;
  private final String propertyValue;

  ConsentReviewDecision(String propertyName, String propertyValue) {
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
