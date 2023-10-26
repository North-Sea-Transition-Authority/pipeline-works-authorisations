package uk.co.ogauthority.pwa.service.enums.workflow.publicnotice;

import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowProperty;

public enum PublicNoticePublicationState implements WorkflowProperty {
  FINISHED("publicationState"),
  WAIT_FOR_START_DATE("publicationState");

  private final String propertyName;

  PublicNoticePublicationState(String propertyName) {
    this.propertyName = propertyName;
  }

  @Override
  public String getPropertyName() {
    return propertyName;
  }

  @Override
  public String getPropertyValue() {
    return this.name();
  }
}
