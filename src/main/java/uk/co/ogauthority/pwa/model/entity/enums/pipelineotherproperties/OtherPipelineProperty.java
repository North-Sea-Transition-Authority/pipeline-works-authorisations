package uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties;

import java.util.Arrays;
import java.util.List;

public enum OtherPipelineProperty {

    WAX_CONTENT("Wax content"),
    WAX_APPEARANCE_TEMPERATURE("Wax appearance temperature"),
    ACID_NUM("Acid number (TAN)"),
    VISCOSITY("Viscosity"),
    DENSITY_GRAVITY("Density gravity"),
    SULPHUR_CONTENT("Sulphur content"),
    POUR_POINT("Pour point"),
    SOLID_CONTENT("Solid content"),
    MERCURY("Mercury"),
    H20("H20");

  private final String displayText;

  OtherPipelineProperty(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<OtherPipelineProperty> asList() {
    return Arrays.asList(OtherPipelineProperty.values());
  }
}
