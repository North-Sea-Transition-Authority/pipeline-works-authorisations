package uk.co.ogauthority.pwa.temp.model.service;

public enum PipelineType {

  PRODUCTION_FLOWLINE("Production Flowline", true),
  PRODUCTION_JUMPER("Production Jumper", false),

  GAS_LIFT_PIPELINE("Gas Lift Pipeline", true),
  GAS_LIFT_JUMPER("Gas Lift Jumper", false),

  METHANOL_PIPELINE("Methanol Pipeline", false),
  SERVICES_UMBILICAL("Services Umbilical", false);

  private String displayName;
  private boolean rootPipelineType;

  PipelineType(String displayName, boolean rootPipelineType) {
    this.displayName = displayName;
    this.rootPipelineType = rootPipelineType;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isRootPipelineType() {
    return rootPipelineType;
  }
}
