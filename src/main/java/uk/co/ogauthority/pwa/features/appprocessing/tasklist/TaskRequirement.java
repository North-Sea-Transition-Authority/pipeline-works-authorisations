package uk.co.ogauthority.pwa.features.appprocessing.tasklist;

import uk.co.ogauthority.pwa.util.enumutils.Displayable;

public enum TaskRequirement implements Displayable {

  REQUIRED("Required tasks", 10),
  OPTIONAL("Optional tasks", 20);

  private final String displayName;
  private final int displayOrder;

  TaskRequirement(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }
}
