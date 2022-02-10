package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocationDetailsSafetyZoneForm {

  private List<String> facilities = new ArrayList<>();


  public List<String> getFacilities() {
    return facilities;
  }

  public void setFacilities(List<String> facilities) {
    this.facilities = facilities;
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

    return Objects.equals(facilities, that.facilities);
  }

  @Override
  public int hashCode() {
    return Objects.hash(facilities);
  }
}
