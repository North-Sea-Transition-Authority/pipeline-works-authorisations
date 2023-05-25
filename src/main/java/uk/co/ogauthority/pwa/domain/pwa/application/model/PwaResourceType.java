package uk.co.ogauthority.pwa.domain.pwa.application.model;

public enum PwaResourceType {
  PETROLEUM("Petroleum", 10),
  HYDROGEN("Hydrogen", 20);

  private final String displayName;

  private final int displayOrder;

  PwaResourceType(String displayName, int displayOrder) {
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
