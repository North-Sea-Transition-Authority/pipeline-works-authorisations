package uk.co.ogauthority.pwa.model.entity.enums.pipelines;

import java.util.stream.Stream;

public enum PipelineType {

  UNKNOWN("Unknown pipeline type", -1, PipelineCoreType.SINGLE_CORE),

  PRODUCTION_FLOWLINE("Production Flowline", 1, PipelineCoreType.SINGLE_CORE),
  PRODUCTION_JUMPER("Production Jumper", 2, PipelineCoreType.SINGLE_CORE),

  GAS_LIFT_PIPELINE("Gas Lift Pipeline", 3, PipelineCoreType.SINGLE_CORE),
  GAS_LIFT_JUMPER("Gas Lift Jumper", 4, PipelineCoreType.SINGLE_CORE),

  WATER_INJECTION_PIPELINE("Water Injection Pipeline", 5, PipelineCoreType.SINGLE_CORE),
  WATER_INJECTION_JUMPER("Water Injection Jumper", 6, PipelineCoreType.SINGLE_CORE),

  METHANOL_PIPELINE("Methanol Pipeline", 7, PipelineCoreType.SINGLE_CORE),
  SERVICES_UMBILICAL("Services Umbilical", 8, PipelineCoreType.MULTI_CORE),

  HYDRAULIC_JUMPER("Hydraulic Jumper", 9, PipelineCoreType.MULTI_CORE),
  CHEMICAL_JUMPER("Chemical Jumper", 10, PipelineCoreType.SINGLE_CORE),
  CONTROL_JUMPER("Control Jumper", 11, PipelineCoreType.SINGLE_CORE),
  UMBILICAL_JUMPER("Umbilical Jumper", 12, PipelineCoreType.MULTI_CORE),
  CABLE("Cable", 13, PipelineCoreType.SINGLE_CORE);

  private String displayName;
  private int displayOrder;
  private PipelineCoreType coreType;

  PipelineType(String displayName, int displayOrder, PipelineCoreType coreType) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.coreType = coreType;
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

  public PipelineCoreType getCoreType() {
    return coreType;
  }

  public void setCoreType(PipelineCoreType coreType) {
    this.coreType = coreType;
  }

  public static Stream<PipelineType> stream() {
    return Stream.of(PipelineType.values());
  }

  public static Stream<PipelineType> streamDisplayValues() {
    return Stream.of(PipelineType.values())
        .filter(pipelineType -> pipelineType.getDisplayOrder() >= 0);
  }

}
