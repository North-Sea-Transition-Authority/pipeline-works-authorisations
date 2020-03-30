package uk.co.ogauthority.pwa.model.entity.enums;

import java.util.Arrays;
import java.util.stream.Stream;

public enum MedianLineStatus {

  NOT_CROSSED(10, "The median line will not be crossed"),
  NEGOTIATIONS_ONGOING(20, "The median line will be crossed and negotiations are ongoing"),
  NEGOTIATIONS_COMPLETED(30, "The median line will be crossed and negotiations have been completed");

  private int displayOrder;
  private String displayText;

  MedianLineStatus(int displayOrder, String displayText) {
    this.displayOrder = displayOrder;
    this.displayText = displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static Stream<MedianLineStatus> stream() {
    return Arrays.stream(MedianLineStatus.values());
  }
}
