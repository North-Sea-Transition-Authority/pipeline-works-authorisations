package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

/**
 * Separate from the pipelines business status, where does it exist in the real world.
 */
public enum  PhysicalPipelineState {

  ON_SEABED(10),
  ONSHORE(20),
  NEVER_EXISTED(30);

  private final int displayOrder;

  PhysicalPipelineState(int displayOrder) {
    this.displayOrder = displayOrder;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

}
