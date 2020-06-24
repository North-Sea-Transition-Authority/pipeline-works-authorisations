package uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties;

import java.util.Arrays;
import java.util.List;

public enum PropertyAvailabilityOption {

    NOT_AVAILABLE("Data not available"),
    NOT_PRESENT("Not present"),
    AVAILABLE("Present / available");

  private final String displayText;

  PropertyAvailabilityOption(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<PropertyAvailabilityOption> asList() {
    return Arrays.asList(PropertyAvailabilityOption.values());
  }
}
