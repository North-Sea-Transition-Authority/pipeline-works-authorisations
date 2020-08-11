package uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;

public enum MaterialType implements DiffableAsString {

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


  @Override
  public String getDiffableString() {
    return this.name();
  }
}
