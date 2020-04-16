package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings;

public class AddCableCrossingForm {

  private String cableName;
  private String location;
  private String cableOwner;

  public String getCableName() {
    return cableName;
  }

  public void setCableName(String cableName) {
    this.cableName = cableName;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getCableOwner() {
    return cableOwner;
  }

  public void setCableOwner(String cableOwner) {
    this.cableOwner = cableOwner;
  }
}
