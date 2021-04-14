package uk.co.ogauthority.pwa.service.enums.workflow.application;

import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowProperty;

/**
 * When an application is being prepared, what values can be set on the workflow?
 * Defined values may be used to trigger conditional transitions.
 */
public enum PwaApplicationSubmitResult implements WorkflowProperty {

  SUBMIT_PREPARED_APPLICATION("prepareApplicationResult", "submit");

  private final String propertyName;

  private final String propertyValue;

  PwaApplicationSubmitResult(String propertyName, String propertyValue) {
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
