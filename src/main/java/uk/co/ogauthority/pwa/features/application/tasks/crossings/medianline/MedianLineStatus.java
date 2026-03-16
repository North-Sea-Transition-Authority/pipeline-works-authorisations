package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.util.enumutils.Displayable;

public enum MedianLineStatus implements Displayable {

  NOT_CROSSED(10, "No median lines will be crossed", "Not crossed"),
  NEGOTIATIONS_ONGOING(20, "Median line will be crossed and negotiations are ongoing", "Negotiations ongoing"),
  NEGOTIATIONS_COMPLETED(30, "Median line will be crossed and negotiations have been completed", "Negotiations completed");

  private final int displayOrder;
  private final String displayText;
  private final String labelText;

  MedianLineStatus(int displayOrder, String displayText, String labelText) {
    this.displayOrder = displayOrder;
    this.displayText = displayText;
    this.labelText = labelText;
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

  public String getLabelText() {
    return labelText;
  }

  public static Stream<MedianLineStatus> stream() {
    return Arrays.stream(MedianLineStatus.values());
  }
}
