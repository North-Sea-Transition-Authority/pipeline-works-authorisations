package uk.co.ogauthority.pwa.temp.model.service.enums;

public enum LongitudeDirection {

  EAST("East"),
  WEST("West");

  private String displayText;

  LongitudeDirection(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }
}
