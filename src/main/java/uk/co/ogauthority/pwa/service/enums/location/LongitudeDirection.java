package uk.co.ogauthority.pwa.service.enums.location;

import java.util.stream.Stream;

public enum LongitudeDirection {

  EAST("East"),
  WEST("West");

  private final String displayText;

  LongitudeDirection(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static Stream<LongitudeDirection> stream() {
    return Stream.of(LongitudeDirection.values());
  }

}