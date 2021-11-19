package uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings;

import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowProperty;

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
