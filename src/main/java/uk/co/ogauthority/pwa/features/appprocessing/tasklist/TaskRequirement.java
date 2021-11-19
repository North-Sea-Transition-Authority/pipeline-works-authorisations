package uk.co.ogauthority.pwa.features.appprocessing.tasklist;

public enum TaskRequirement {

  REQUIRED("Required tasks", 10),
  OPTIONAL("Optional tasks", 20);

  private final String displayName;
  private final int displayOrder;

  TaskRequirement(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
