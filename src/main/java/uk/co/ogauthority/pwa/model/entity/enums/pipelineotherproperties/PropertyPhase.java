package uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum PropertyPhase {

    OIL("Oil"),
    CONDENSATE("Condensate"),
    GAS("Gas"),
    WATER("Water"),
    OTHER("Other");

  private final String displayText;

  PropertyPhase(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<PropertyPhase> asList() {
    return Arrays.asList(PropertyPhase.values());
  }

  public static Stream<PropertyPhase> stream() {
    return Arrays.stream(PropertyPhase.values());
  }
}
