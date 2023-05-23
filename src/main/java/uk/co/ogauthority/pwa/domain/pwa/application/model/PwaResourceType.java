package uk.co.ogauthority.pwa.domain.pwa.application.model;

public enum PwaResourceType {
  PETROLEUM("Petroleum"),
  HYDROGEN("Hydrogen");

  private final String displayName;

  PwaResourceType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
