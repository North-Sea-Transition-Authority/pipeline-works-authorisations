package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.util.enumutils.Displayable;

public enum PsrNotification implements Displayable {

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

  @Override
  public String getDisplayName() {
    return displayText;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<PsrNotification> stream() {
    return Arrays.stream(PsrNotification.values());
  }
}
