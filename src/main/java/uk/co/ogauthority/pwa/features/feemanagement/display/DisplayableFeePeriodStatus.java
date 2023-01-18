package uk.co.ogauthority.pwa.features.feemanagement.display;

public enum DisplayableFeePeriodStatus {
  ACTIVE("Active", "govuk-tag--turquoise"),
  PENDING("Pending", "govuk-tag--blue"),
  COMPLETE("Complete", "govuk-tag--grey");



  private String displayStatus;

  private String tagClass;

  DisplayableFeePeriodStatus(String displayStatus, String tagClass) {
    this.displayStatus = displayStatus;
    this.tagClass = tagClass;
  }

  public String getDisplayStatus() {
    return displayStatus;
  }

  public String getTagClass() {
    return tagClass;
  }
}
