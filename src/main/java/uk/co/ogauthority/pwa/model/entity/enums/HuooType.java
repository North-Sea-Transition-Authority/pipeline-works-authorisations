package uk.co.ogauthority.pwa.model.entity.enums;

import java.util.Arrays;
import java.util.stream.Stream;

public enum HuooType {

  PORTAL_ORG(10, "Legal entity"),
  TREATY_AGREEMENT(20, "Treaty agreement");

  private int displayOrder;
  private String displayText;

  HuooType(int displayOrder, String displayText) {
    this.displayOrder = displayOrder;
    this.displayText = displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static Stream<HuooType> stream() {
    return Arrays.stream(HuooType.values());
  }
}
