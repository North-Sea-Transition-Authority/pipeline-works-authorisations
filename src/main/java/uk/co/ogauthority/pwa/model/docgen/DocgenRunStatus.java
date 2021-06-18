package uk.co.ogauthority.pwa.model.docgen;

public enum DocgenRunStatus {

  PENDING("Pending"),
  COMPLETE("Completed"),
  FAILED("Failed");

  private final String displayName;

  DocgenRunStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

}
