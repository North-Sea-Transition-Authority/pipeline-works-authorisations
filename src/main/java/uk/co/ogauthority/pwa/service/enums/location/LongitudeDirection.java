package uk.co.ogauthority.pwa.service.enums.location;

import java.util.stream.Stream;

public enum LongitudeDirection {

  EAST("East", "E"),
  WEST("West", "W");

  private final String displayText;
  private final String displayTextShort;

  LongitudeDirection(String displayText, String displayTextShort) {
    this.displayText = displayText;
    this.displayTextShort = displayTextShort;
  }

  public String getDisplayText() {
    return displayText;
  }

  public String getDisplayTextShort() {
    return displayTextShort;
  }

  public static Stream<LongitudeDirection> stream() {
    return Stream.of(LongitudeDirection.values());
  }

}