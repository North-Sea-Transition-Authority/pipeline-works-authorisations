package uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties;

import java.util.Arrays;
import java.util.List;

public enum OtherPipelineProperty {

    WAX_CONTENT("Wax content", "weight %"),
    WAX_APPEARANCE_TEMPERATURE("Wax appearance temperature", "°C"),
    ACID_NUM("Acid number (TAN)", "< mg KOH/g"),
    VISCOSITY("Viscosity", "bar(a)"),
    DENSITY_GRAVITY("Density/gravity", "kg/m3"),
    SULPHUR_CONTENT("Sulphur content", "weight %"),
    POUR_POINT("Pour point", "°C"),
    SOLID_CONTENT("Solid content", "weight %"),
    MERCURY("Mercury", "μg/m3"),
    H20("H20", "ppm");

  private final String displayText;
  private final String unitMeasurement;

  OtherPipelineProperty(String displayText, String unitMeasurement) {
    this.displayText = displayText;
    this.unitMeasurement = unitMeasurement;
  }

  public String getDisplayText() {
    return displayText;
  }

  public String getUnitMeasurement() {
    return unitMeasurement;
  }

  public static List<OtherPipelineProperty> asList() {
    return Arrays.asList(OtherPipelineProperty.values());
  }
}
