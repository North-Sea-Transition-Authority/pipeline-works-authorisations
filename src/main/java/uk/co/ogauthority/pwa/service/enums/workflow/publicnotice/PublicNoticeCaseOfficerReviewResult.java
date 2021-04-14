package uk.co.ogauthority.pwa.service.enums.workflow.publicnotice;

import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowProperty;

/**
 * Defines workflow property values associated with a public notice in case officer review stage.
 */
public enum PublicNoticeCaseOfficerReviewResult implements WorkflowProperty {

  UPDATE_REQUESTED("caseOfficerReviewDecision", "UPDATE_REQUESTED"),
  PUBLICATION_STARTED("caseOfficerReviewDecision", "PUBLICATION_STARTED"),
  WAIT_FOR_START_DATE("caseOfficerReviewDecision", "WAIT_FOR_START_DATE");

  private final String propertyName;

  private final String propertyValue;

  PublicNoticeCaseOfficerReviewResult(String propertyName, String propertyValue) {
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
