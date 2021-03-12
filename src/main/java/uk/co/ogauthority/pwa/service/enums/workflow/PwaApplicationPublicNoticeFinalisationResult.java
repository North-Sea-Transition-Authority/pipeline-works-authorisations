package uk.co.ogauthority.pwa.service.enums.workflow;

/**
 * Defines workflow property values associated with a finalised public notice.
 */
public enum PwaApplicationPublicNoticeFinalisationResult implements WorkflowProperty {

  PUBLICATION_STARTED("startPublicationDecision", "PUBLICATION_STARTED"),
  WAIT_FOR_START_DATE("startPublicationDecision", "WAIT_FOR_START_DATE");

  private final String propertyName;

  private final String propertyValue;

  PwaApplicationPublicNoticeFinalisationResult(String propertyName, String propertyValue) {
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
