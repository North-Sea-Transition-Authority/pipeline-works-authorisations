package uk.co.ogauthority.pwa.model.entity.enums.publicnotice;

public enum PublicNoticeStatus {

  DRAFT("Draft"),
  MANAGER_APPROVAL("Manager approval"),
  APPLICANT_UPDATE("Applicant update"),
  CASE_OFFICER_REVIEW("Case officer review"),
  FINALISATION("Finalisation"),
  WITHDRAWN("Withdrawn");

  private final String displayText;

  PublicNoticeStatus(String displayText) {
    this.displayText = displayText;
  }


  public String getDisplayText() {
    return displayText;
  }
}
