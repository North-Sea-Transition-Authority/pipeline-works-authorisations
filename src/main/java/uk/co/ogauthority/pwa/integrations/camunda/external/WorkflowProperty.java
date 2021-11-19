package uk.co.ogauthority.pwa.integrations.camunda.external;

/**
 * Workflows allow information to be set and decisions to be made to through task variables and workflow properties.
 */
public interface WorkflowProperty {

  String getPropertyName();

  String getPropertyValue();
}
