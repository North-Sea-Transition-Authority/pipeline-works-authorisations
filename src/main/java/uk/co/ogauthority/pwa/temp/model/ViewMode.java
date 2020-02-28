package uk.co.ogauthority.pwa.temp.model;

public enum ViewMode {

  UPDATE("Update"),
  NEW("New");

  private String displayText;

  ViewMode(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }
}
