package uk.co.ogauthority.pwa.service.enums.workflow;

/**
 * Defines workflow property values associated with a case officer's decision on a public notice document requiring an update.
 */
public enum PwaApplicationPublicNoticeDocumentResult implements WorkflowProperty {

  UPDATE_REQUESTED("documentUpdateDecision", "UPDATE_REQUESTED"),
  ACCEPTED("documentUpdateDecision", "UPDATE_NOT_REQUESTED");

  private final String propertyName;

  private final String propertyValue;

  PwaApplicationPublicNoticeDocumentResult(String propertyName, String propertyValue) {
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
