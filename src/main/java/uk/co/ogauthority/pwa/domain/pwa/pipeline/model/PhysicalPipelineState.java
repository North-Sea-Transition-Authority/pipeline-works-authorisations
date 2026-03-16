package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import uk.co.ogauthority.pwa.util.enumutils.Displayable;

/**
 * Separate from the pipelines business status, where does it exist in the real world.
 */
public enum  PhysicalPipelineState implements Displayable {

  ON_SEABED(10),
  ONSHORE(20),
  NEVER_EXISTED(30);

  private final int displayOrder;

  PhysicalPipelineState(int displayOrder) {
    this.displayOrder = displayOrder;
  }

  @Override
  public String getDisplayName() {
    return "";
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

}
