package uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits;

import java.util.Arrays;
import java.util.List;

public enum MaterialType {

    CONCRETE_MATTRESSES("Concrete mattresses"),
    ROCK("Rock"),
    GROUT_BAGS("Grout bags"),
    OTHER("Other");

  private final String displayText;

  MaterialType(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<MaterialType> asList() {
    return Arrays.asList(MaterialType.values());
  }
}
