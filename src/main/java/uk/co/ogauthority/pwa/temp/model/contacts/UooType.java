package uk.co.ogauthority.pwa.temp.model.contacts;

public enum UooType {

  AGREEMENT("Agreement"),
  COMPANY("Company");

  private String displayText;

  UooType(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }
}
