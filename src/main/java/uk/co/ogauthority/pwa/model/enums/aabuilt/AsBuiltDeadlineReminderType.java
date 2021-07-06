package uk.co.ogauthority.pwa.model.enums.aabuilt;

public enum AsBuiltDeadlineReminderType {

  DEADLINE_UPCOMING("upcoming deadline"),
  DEADLINE_PASSED("past deadline");

  private final String deadlineTypeText;

  AsBuiltDeadlineReminderType(String deadlineTypeText) {
    this.deadlineTypeText = deadlineTypeText;
  }

  public String getDeadlineTypeText() {
    return deadlineTypeText;
  }

}
