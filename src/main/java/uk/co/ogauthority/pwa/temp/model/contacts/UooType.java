package uk.co.ogauthority.pwa.temp.model.contacts;

public enum UooType {

  TREATY("Treaty"),
  COMPANY("Company");

  private String displayText;

  UooType(String displayText) {
    this.displayText = displayText;
  }

  @Override
  public String toString() {
    return displayText;
  }
}
