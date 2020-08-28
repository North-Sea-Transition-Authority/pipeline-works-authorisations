package uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition;

import java.util.Arrays;
import java.util.List;

public enum Chemical {

  H2S("H₂S"),
  CO2("CO₂"),
  H2O("H₂O"),
  N2("N₂"),
  C1("C1"),
  C2("C2"),
  C3("C3"),
  C4("C4"),
  C5("C5"),
  C6("C6"),
  C7("C7"),
  C8("C8"),
  C9("C9"),
  C10("C10"),
  C11("C11"),
  C12_PLUS("C12+");

  private final String displayText;

  Chemical(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<Chemical> asList() {
    return Arrays.asList(Chemical.values());
  }

}
