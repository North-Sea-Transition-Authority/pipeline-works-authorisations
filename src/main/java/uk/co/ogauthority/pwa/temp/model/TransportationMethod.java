package uk.co.ogauthority.pwa.temp.model;

public enum TransportationMethod {

  VESSEL("Vessel"),
  OTHER("Other");

  private String displayName;

  TransportationMethod(String displayName) {
    this.displayName = displayName;
  }


  @Override
  public String toString() {
    return this.displayName;
  }
}
