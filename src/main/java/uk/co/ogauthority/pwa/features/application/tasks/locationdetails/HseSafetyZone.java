package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import java.util.Arrays;
import java.util.stream.Stream;

public enum HseSafetyZone {

  YES(10, "Yes"),
  PARTIALLY(20, "Partially"),
  NO(30, "No");

  private int displayOrder;
  private String displayText;

  HseSafetyZone(int displayOrder, String displayText) {
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
