package uk.co.ogauthority.pwa.service.enums.projectinformation;

public enum PermanentDeposits {

  THIS_APP("This Application"),
  LATER_APP("Later Application"),
  NONE("None");

  private final String displayText;

  PermanentDeposits(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }
}
