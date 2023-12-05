package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;

public enum OtherPipelineProperty {

    WAX_CONTENT(
        "Wax content",
        UnitMeasurement.PERCENTAGE_WEIGHT,
        List.of(PwaResourceType.HYDROGEN, PwaResourceType.PETROLEUM)),
    WAX_APPEARANCE_TEMPERATURE(
        "Wax appearance temperature",
        UnitMeasurement.DEGREES_CELSIUS,
        List.of(PwaResourceType.HYDROGEN, PwaResourceType.PETROLEUM)),
    ACID_NUM(
        "Acid number (TAN)",
        UnitMeasurement.ACID_NUMBER,
        List.of(PwaResourceType.HYDROGEN, PwaResourceType.PETROLEUM)),
    VISCOSITY(
        "Viscosity",
        UnitMeasurement.CENTIPOISE),
    DENSITY_GRAVITY(
        "Density/gravity",
        UnitMeasurement.KG_METRE_CUBED),
    SULPHUR_CONTENT(
        "Sulphur content",
        UnitMeasurement.PERCENTAGE_WEIGHT,
        List.of(PwaResourceType.HYDROGEN, PwaResourceType.PETROLEUM)),
    POUR_POINT(
        "Pour point",
        UnitMeasurement.DEGREES_CELSIUS,
        List.of(PwaResourceType.HYDROGEN, PwaResourceType.PETROLEUM)),
    SOLID_CONTENT(
        "Solid content",
        UnitMeasurement.PERCENTAGE_WEIGHT),
    MERCURY(
        "Mercury",
        UnitMeasurement.MICROGRAM_METRE_CUBED,
        List.of(PwaResourceType.HYDROGEN, PwaResourceType.PETROLEUM)),
    CRITICAL_TEMP(
        "Critical temperature",
        UnitMeasurement.DEGREES_CELSIUS,
        List.of(PwaResourceType.CCUS)
    ),
    CRITICAL_PRESSURE(
        "Critical pressure",
        UnitMeasurement.BAR_A,
        List.of(PwaResourceType.CCUS)
    ),
    CO2_CRITICAL_DENSITY(
        "CO₂ critical density",
        UnitMeasurement.KG_METRE_CUBED,
        List.of(PwaResourceType.CCUS)
    );

  private final String displayText;
  private final UnitMeasurement unitMeasurement;

  private final List<PwaResourceType> applicableResourceType;

  OtherPipelineProperty(String displayText, UnitMeasurement unitMeasurement) {
    this.displayText = displayText;
    this.unitMeasurement = unitMeasurement;
    this.applicableResourceType = PwaResourceType.getAll();
  }

  OtherPipelineProperty(String displayText, UnitMeasurement unitMeasurement, List<PwaResourceType> applicableResourceType) {
    this.displayText = displayText;
    this.unitMeasurement = unitMeasurement;
    this.applicableResourceType = applicableResourceType;
  }

  public String getDisplayText() {
    return displayText;
  }

  public UnitMeasurement getUnitMeasurement() {
    return unitMeasurement;
  }

  public List<PwaResourceType> getApplicableResourceType() {
    return applicableResourceType;
  }

  public static List<OtherPipelineProperty> asList() {
    return Arrays.asList(OtherPipelineProperty.values());
  }

  public static List<OtherPipelineProperty> asList(PwaResourceType resourceType) {
    return asList().stream()
        .filter(property -> property.getApplicableResourceType().contains(resourceType))
        .collect(Collectors.toList());
  }
}
