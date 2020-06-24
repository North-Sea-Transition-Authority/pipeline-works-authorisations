package uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition;

import java.util.Arrays;
import java.util.List;

public enum FluidCompositionOption {

    NONE("Not present"),
    TRACE("Trace (less than 0.01 mole %)"),
    HIGHER_AMOUNT("Provide mole %");

  private final String displayText;

  FluidCompositionOption(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<FluidCompositionOption> asList() {
    return Arrays.asList(FluidCompositionOption.values());
  }
}
