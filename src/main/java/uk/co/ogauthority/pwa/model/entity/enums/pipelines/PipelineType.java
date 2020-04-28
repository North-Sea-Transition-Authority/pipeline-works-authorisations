package uk.co.ogauthority.pwa.model.entity.enums.pipelines;

import java.util.stream.Stream;

public enum PipelineType {

  PRODUCTION_FLOWLINE("Production Flowline", 1),
  PRODUCTION_JUMPER("Production Jumper", 2),

  GAS_LIFT_PIPELINE("Gas Lift Pipeline", 3),
  GAS_LIFT_JUMPER("Gas Lift Jumper", 4),

  WATER_INJECTION_PIPELINE("Water Injection Pipeline", 5),
  WATER_INJECTION_JUMPER("Water Injection Jumper", 6),

  METHANOL_PIPELINE("Methanol Pipeline", 7),
  SERVICES_UMBILICAL("Services Umbilical", 8),

  HYDRAULIC_JUMPER("Hydraulic Jumper", 9),
  CHEMICAL_JUMPER("Chemical Jumper", 10),
  CONTROL_JUMPER("Control Jumper", 11);

  private String displayName;
  private int displayOrder;

  PipelineType(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }

  public static Stream<PipelineType> stream() {
    return Stream.of(PipelineType.values());
  }

}
