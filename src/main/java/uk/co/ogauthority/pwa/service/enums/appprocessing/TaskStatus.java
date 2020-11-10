package uk.co.ogauthority.pwa.service.enums.appprocessing;

public enum TaskStatus {

  NOT_STARTED("Not started"),
  IN_PROGRESS("In progress"),
  NOT_REQUIRED("Not required"),
  COMPLETED("Completed", "fds-task-list__task-completed"),
  NOT_COMPLETED("Not completed", "govuk-tag--grey fds-task-list__task-not-completed");

  private final String displayText;
  private final String tagClass;

  TaskStatus(String displayText) {
    this.displayText = displayText;
    this.tagClass = null;
  }

  TaskStatus(String displayText, String tagClass) {
    this.displayText = displayText;
    this.tagClass = tagClass;
  }

  public String getDisplayText() {
    return displayText;
  }

  public String getTagClass() {
    return tagClass;
  }
}