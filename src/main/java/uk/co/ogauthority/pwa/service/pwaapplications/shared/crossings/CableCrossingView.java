package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossing;

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
