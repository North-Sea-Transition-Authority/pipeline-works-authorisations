package uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties;

import java.util.Arrays;
import java.util.List;

public enum PropertyUnitMeasurement {

  WAX_CONTENT("weight %"),
  WAX_APPEARANCE_TEMPERATURE("°C"),
  ACID_NUM("< mg KOH/g"),
  VISCOSITY("bar(a)"),
  DENSITY_GRAVITY("kg/m3"),
  SULPHUR_CONTENT("weight %"),
  POUR_POINT("°C"),
  SOLID_CONTENT("weight %"),
  MERCURY("μg/m3"),
  H20("ppm");

  private final String displayText;

  PropertyUnitMeasurement(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<PropertyUnitMeasurement> asList() {
    return Arrays.asList(PropertyUnitMeasurement.values());
  }
}
