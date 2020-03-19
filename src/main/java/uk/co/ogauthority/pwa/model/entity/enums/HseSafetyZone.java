package uk.co.ogauthority.pwa.model.entity.enums;

import java.util.Arrays;
import java.util.stream.Stream;

public enum HseSafetyZone {

  YES("Yes", 10),
  PARTIALLY("Partially", 20),
  NO("No", 30);

  private String displayText;
  private int displayOrder;

  HseSafetyZone(String displayText, int displayOrder) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<HseSafetyZone> stream() {
    return Arrays.stream(HseSafetyZone.values());
  }
}
