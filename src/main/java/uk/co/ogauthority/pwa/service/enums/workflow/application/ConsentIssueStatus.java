package uk.co.ogauthority.pwa.service.enums.workflow.application;

import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowProperty;

/**
 * Defines workflow property values associated with a consent issue status.
 */
public enum ConsentIssueStatus implements WorkflowProperty {

  COMPLETE("consentIssueStatus", "COMPLETE"),
  FAILED("consentIssueStatus", "FAILED");

  private final String propertyName;
  private final String propertyValue;

  ConsentIssueStatus(String propertyName, String propertyValue) {
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
