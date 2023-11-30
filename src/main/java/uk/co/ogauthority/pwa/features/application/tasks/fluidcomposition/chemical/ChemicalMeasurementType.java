package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical;

import java.util.Arrays;
import java.util.List;

public enum ChemicalMeasurementType {

  NONE("Not present", "Not present", false),
  TRACE("Trace (less than 0.01 mole %)", "Trace", false),
  PPMV_100K("Present", "ppmv", true, 100000, 0),
  MOLE_PERCENTAGE("Present", "More than trace", true, 100, 0);

  private final String displayText;
  private final String displayTextSimple;

  private final boolean hasNestedInput;

  private final Integer upperLimit;

  private final Integer lowerLimit;

  ChemicalMeasurementType(String displayText, String simpleDisplayText, boolean hasNestedInput) {
    this.displayText = displayText;
    this.displayTextSimple = simpleDisplayText;
    this.hasNestedInput = hasNestedInput;
    upperLimit = null;
    lowerLimit = null;
  }

  ChemicalMeasurementType(String displayText, String simpleDisplayText, boolean hasNestedInput, int upperLimit, int lowerLimit) {
    this.displayText = displayText;
    this.displayTextSimple = simpleDisplayText;
    this.hasNestedInput = hasNestedInput;
    this.upperLimit = upperLimit;
    this.lowerLimit = lowerLimit;
  }

  public String getDisplayText() {
    return displayText;
  }

  public String getDisplayTextSimple() {
    return displayTextSimple;
  }

  public int getUpperLimit() {
    return upperLimit;
  }

  public int getLowerLimit() {
    return lowerLimit;
  }

  public boolean hasNestedInput() {
    return hasNestedInput;
  }

  public static List<ChemicalMeasurementType> asList() {
    return Arrays.asList(ChemicalMeasurementType.values());
  }
}
