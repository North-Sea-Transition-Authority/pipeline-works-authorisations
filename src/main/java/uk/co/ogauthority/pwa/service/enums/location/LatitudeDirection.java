package uk.co.ogauthority.pwa.service.enums.location;

public enum LatitudeDirection {

  NORTH("North"),
  SOUTH("South");

  private final String displayText;

  LatitudeDirection(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }
}
