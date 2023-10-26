package uk.co.ogauthority.pwa.model.view.publicnotice;

public enum PublicNoticeEventType {

  APPROVED("Review approved", "Approved by"),
  DOCUMENT_CREATED("New document uploaded", "Uploaded by"),
  ENDED("Public notice ended", "Ended by"),
  PUBLISHED("Public notice published", "Published by"),
  PUBLICATION_DATES_SET("Publication dates set", "Published by"),
  REJECTED("Review rejected", "Rejected by"),
  REQUEST_CREATED("Review requested", "Requested by"),
  WITHDRAWN("Public notice withdrawn", "Withdrawn by");

  final String displayText;

  final String actionText;

  PublicNoticeEventType(String displayText, String actionText) {
    this.displayText = displayText;
    this.actionText = actionText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public String getActionText() {
    return actionText;
  }
}
