package uk.co.ogauthority.pwa.service.enums.appprocessing;

public enum TaskStatus {

  NOT_STARTED("Not started"),
  IN_PROGRESS("In progress"),
  NOT_REQUIRED("Not required"),
  COMPLETED("Completed");

  private final String displayText;

  TaskStatus(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }
}