package uk.co.ogauthority.pwa.temp.model.admindetails;

public enum WithinSafetyZone {

  YES("Yes"),
  PARTIALLY("Partially"),
  NO("No");

  private String displayText;

  WithinSafetyZone(String displayText) {
    this.displayText = displayText;
  }

  @Override
  public String toString() {
    return displayText;
  }
}
