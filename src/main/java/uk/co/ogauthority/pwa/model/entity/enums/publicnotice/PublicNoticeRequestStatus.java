package uk.co.ogauthority.pwa.model.entity.enums.publicnotice;

public enum PublicNoticeRequestStatus {

  WAITING_MANAGER_APPROVAL("Waiting for manager approval"),
  REJECTED("Rejected"),
  APPROVED("Approved");

  private final String displayText;

  PublicNoticeRequestStatus(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

}
