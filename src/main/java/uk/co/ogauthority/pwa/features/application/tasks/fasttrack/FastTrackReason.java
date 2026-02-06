package uk.co.ogauthority.pwa.features.application.tasks.fasttrack;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.util.enumutils.Displayable;

public enum FastTrackReason implements Displayable {

  AVOID_ENVIRONMENTAL_DISASTER(10, "Avoiding environmental disaster"),
  SAVING_BARRELS(20, "Saving barrels"),
  PROJECT_PLANNING(30, "Project planning"),
  OTHER_REASON(40, "Other");

  private final int displayOrder;
  private final String displayText;


  FastTrackReason(int displayOrder, String displayText) {
    this.displayOrder = displayOrder;
    this.displayText = displayText;
  }

  @Override
  public String getDisplayName() {
    return displayText;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static Stream<FastTrackReason> stream() {
    return Arrays.stream(FastTrackReason.values());
  }
}
