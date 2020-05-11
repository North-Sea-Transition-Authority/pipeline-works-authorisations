package uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings;

import java.util.Arrays;
import java.util.stream.Stream;

public enum CrossingAgreementTask {

  LICENCE_AND_BLOCK_NUMBERS("Licence and block numbers", 10),
  CROSSING_TYPES("Types of crossings", 20),
  CABLE_CROSSINGS("Cable crossings", 40),
  MEDIAN_LINE("Median line crossings", 50);

  private String displayText;
  private int displayOrder;

  CrossingAgreementTask(String displayText, int displayOrder) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<CrossingAgreementTask> stream() {
    return Arrays.stream(CrossingAgreementTask.values());
  }
}
