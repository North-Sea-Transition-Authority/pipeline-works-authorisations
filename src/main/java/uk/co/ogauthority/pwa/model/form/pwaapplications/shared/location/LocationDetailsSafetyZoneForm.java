package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;

public class LocationDetailsSafetyZoneForm {

  private HseSafetyZone withinSafetyZone;
  private List<String> facilitiesIfYes = new ArrayList<>();
  private List<String> facilitiesIfPartially = new ArrayList<>();



  public HseSafetyZone getWithinSafetyZone() {
    return withinSafetyZone;
  }

  public void setWithinSafetyZone(HseSafetyZone withinSafetyZone) {
    this.withinSafetyZone = withinSafetyZone;
  }

  public List<String> getFacilitiesIfYes() {
    return facilitiesIfYes;
  }

  public void setFacilitiesIfYes(List<String> facilitiesIfYes) {
    this.facilitiesIfYes = facilitiesIfYes;
  }

  public List<String> getFacilitiesIfPartially() {
    return facilitiesIfPartially;
  }

  public void setFacilitiesIfPartially(List<String> facilitiesIfPartially) {
    this.facilitiesIfPartially = facilitiesIfPartially;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationDetailsSafetyZoneForm that = (LocationDetailsSafetyZoneForm) o;
    return withinSafetyZone == that.withinSafetyZone
        && Objects.equals(facilitiesIfYes, that.facilitiesIfYes)
        && Objects.equals(facilitiesIfPartially, that.facilitiesIfPartially);
  }

  @Override
  public int hashCode() {
    return Objects.hash(withinSafetyZone, facilitiesIfYes, facilitiesIfPartially);
  }
}
