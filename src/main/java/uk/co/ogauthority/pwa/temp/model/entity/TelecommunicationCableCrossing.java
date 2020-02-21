package uk.co.ogauthority.pwa.temp.model.entity;

public class TelecommunicationCableCrossing {

  private String cableNameOrLocation;
  private String holderOfCable;

  public TelecommunicationCableCrossing(String cableNameOrLocation, String holderOfCable) {
    this.cableNameOrLocation = cableNameOrLocation;
    this.holderOfCable = holderOfCable;
  }

  public String getCableNameOrLocation() {
    return cableNameOrLocation;
  }

  public void setCableNameOrLocation(String cableNameOrLocation) {
    this.cableNameOrLocation = cableNameOrLocation;
  }

  public String getHolderOfCable() {
    return holderOfCable;
  }

  public void setHolderOfCable(String holderOfCable) {
    this.holderOfCable = holderOfCable;
  }
}
