package uk.co.ogauthority.pwa.service.enums.appprocessing;

public enum TaskStatus {

  NOT_STARTED("Not started", "govuk-tag--grey"),
  IN_PROGRESS("In progress"),
  NOT_REQUIRED("Not required", null, true),
  CANNOT_START_YET("Cannot start yet", "govuk-tag--grey", true),
  COMPLETED("Completed", "fds-task-list__task-completed"),
  NOT_COMPLETED("Not completed", "govuk-tag--grey fds-task-list__task-not-completed");

  private final String displayText;
  private final String tagClass;
  private final boolean forceInaccessible;

  TaskStatus(String displayText) {
    this(displayText, null, false);
  }

  TaskStatus(String displayText, String tagClass) {
    this(displayText, tagClass, false);
  }

  TaskStatus(String displayText, String tagClass, boolean forceInaccessible) {
    this.displayText = displayText;
    this.tagClass = tagClass;
    this.forceInaccessible = forceInaccessible;
  }

  public String getDisplayText() {
    return displayText;
  }

  public String getTagClass() {
    return tagClass;
  }

  public boolean shouldForceInaccessible() {
    return forceInaccessible;
  }
}