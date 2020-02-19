package uk.co.ogauthority.pwa.temp.model.service.enums;

public enum LatitudeDirection {

  NORTH("North"),
  SOUTH("South");

  private String displayText;

  LatitudeDirection(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }
}
