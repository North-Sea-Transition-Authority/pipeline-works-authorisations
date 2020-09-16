package uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition;

import java.util.Arrays;
import java.util.List;

public enum FluidCompositionOption {

  NONE("Not present", "Not present"),
  TRACE("Trace (less than 0.01 mole %)", "Trace"),
  HIGHER_AMOUNT("0.01 mole % or more", "More than trace");



  private final String displayText;
  private final String displayTextSimple;

  FluidCompositionOption(String displayText, String simpleDisplayText) {
    this.displayText = displayText;
    this.displayTextSimple = simpleDisplayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public String getDisplayTextSimple() {
    return displayTextSimple;
  }

  public static List<FluidCompositionOption> asList() {
    return Arrays.asList(FluidCompositionOption.values());
  }
}
