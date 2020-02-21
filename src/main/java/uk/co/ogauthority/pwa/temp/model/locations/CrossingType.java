package uk.co.ogauthority.pwa.temp.model.locations;

public enum CrossingType {

  BLOCK("Block"),
  TELECOMMUNICATION("Telecommunication"),
  PIPELINE("Pipeline");

  private String displayText;

  CrossingType(String displayText) {
    this.displayText = displayText;
  }

  @Override
  public String toString() {
    return displayText;
  }
}
