package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import java.util.Arrays;
import java.util.stream.Stream;

public enum PsrNotification {

  YES(10, "Yes"),
  NO(20, "Not yet"),
  NOT_REQUIRED(30, "A PSR notification is not required");

  private int displayOrder;
  private String displayText;

  PsrNotification(int displayOrder, String displayText) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<PsrNotification> stream() {
    return Arrays.stream(PsrNotification.values());
  }
}
