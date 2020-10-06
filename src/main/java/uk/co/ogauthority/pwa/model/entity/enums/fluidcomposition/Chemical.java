package uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition;

import java.util.Arrays;
import java.util.List;

public enum Chemical {

  H2S("H₂S", 1),
  CO2("CO₂", 2),
  H2O("H₂O", 3),
  N2("N₂", 4),
  C1("C1", 5),
  C2("C2", 6),
  C3("C3", 7),
  C4("C4", 8),
  C5("C5", 9),
  C6("C6", 10),
  C7("C7", 11),
  C8("C8", 12),
  C9("C9", 13),
  C10("C10", 14),
  C11("C11", 15),
  C12_PLUS("C12+", 16);

  private final String displayText;
  private final int displayOrder;

  Chemical(String displayText, int displayOrder) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static List<Chemical> asList() {
    return Arrays.asList(Chemical.values());
  }

}
