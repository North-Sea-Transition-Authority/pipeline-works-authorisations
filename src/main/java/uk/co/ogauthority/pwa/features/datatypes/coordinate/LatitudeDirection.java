package uk.co.ogauthority.pwa.features.datatypes.coordinate;

public enum LatitudeDirection {

  NORTH("North", "N"),
  SOUTH("South", "S");

  private final String displayText;
  private final String displayTextShort;

  LatitudeDirection(String displayText, String displayTextShort) {
    this.displayText = displayText;
    this.displayTextShort = displayTextShort;
  }

  public String getDisplayText() {
    return displayText;
  }

  public String getDisplayTextShort() {
    return displayTextShort;
  }
}
