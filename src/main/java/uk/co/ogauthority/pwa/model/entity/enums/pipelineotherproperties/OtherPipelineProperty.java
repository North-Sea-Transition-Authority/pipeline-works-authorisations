package uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;

public enum OtherPipelineProperty {

    WAX_CONTENT("Wax content", UnitMeasurement.PERCENTAGE_WEIGHT),
    WAX_APPEARANCE_TEMPERATURE("Wax appearance temperature", UnitMeasurement.DEGREES_CELSIUS),
    ACID_NUM("Acid number (TAN)", UnitMeasurement.ACID_NUMBER),
    VISCOSITY("Viscosity", UnitMeasurement.CENTIPOISE),
    DENSITY_GRAVITY("Density/gravity", UnitMeasurement.KG_METRE_CUBED),
    SULPHUR_CONTENT("Sulphur content", UnitMeasurement.PERCENTAGE_WEIGHT),
    POUR_POINT("Pour point", UnitMeasurement.DEGREES_CELSIUS),
    SOLID_CONTENT("Solid content", UnitMeasurement.PERCENTAGE_WEIGHT),
    MERCURY("Mercury", UnitMeasurement.MICROGRAM_METRE_CUBED);

  private final String displayText;
  private final UnitMeasurement unitMeasurement;

  OtherPipelineProperty(String displayText, UnitMeasurement unitMeasurement) {
    this.displayText = displayText;
    this.unitMeasurement = unitMeasurement;
  }

  public String getDisplayText() {
    return displayText;
  }

  public UnitMeasurement getUnitMeasurement() {
    return unitMeasurement;
  }

  public static List<OtherPipelineProperty> asList() {
    return Arrays.asList(OtherPipelineProperty.values());
  }
}
