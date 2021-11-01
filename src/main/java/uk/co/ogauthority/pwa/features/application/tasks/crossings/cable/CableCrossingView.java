package uk.co.ogauthority.pwa.features.application.tasks.crossings.cable;

public class CableCrossingView {

  private String owner;
  private String cableName;
  private String location;
  private Integer id;

  public CableCrossingView(PadCableCrossing cableCrossing) {
    owner = cableCrossing.getCableOwner();
    cableName = cableCrossing.getCableName();
    location = cableCrossing.getLocation();
    id = cableCrossing.getId();
  }

  public String getOwner() {
    return owner;
  }

  public String getCableName() {
    return cableName;
  }

  public String getLocation() {
    return location;
  }

  public Integer getId() {
    return id;
  }
}
