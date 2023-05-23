package uk.co.ogauthority.pwa.model.enums;

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

  public String getAppendixHyphen() {
    return " - " + getDisplayName();
  }

  public String getAppendixBracket() {
    return " (" + getDisplayName() + ")";
  }
}
