package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.validator.constraints.Length;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;

public class LocationDetailsForm {

  @Length(max = 4000, message = "Approximate project location from shore must be 4000 characters or fewer")
  private String approximateProjectLocationFromShore;
  private HseSafetyZone withinSafetyZone;
  private List<DevukFacility> facilitiesIfYes;
  private List<DevukFacility> facilitiesIfPartially;
  private Boolean facilitiesOffshore;
  private Boolean transportsMaterialsToShore;

  @Length(max = 4000, message = "Transportation method must be 4000 characters or fewer")
  private String transportationMethod;

  public LocationDetailsForm() {
    facilitiesIfPartially = new ArrayList<>();
    facilitiesIfYes = new ArrayList<>();
  }

  public String getApproximateProjectLocationFromShore() {
    return approximateProjectLocationFromShore;
  }

  public void setApproximateProjectLocationFromShore(String approximateProjectLocationFromShore) {
    this.approximateProjectLocationFromShore = approximateProjectLocationFromShore;
  }

  public HseSafetyZone getWithinSafetyZone() {
    return withinSafetyZone;
  }

  public void setWithinSafetyZone(HseSafetyZone withinSafetyZone) {
    this.withinSafetyZone = withinSafetyZone;
  }

  public List<DevukFacility> getFacilitiesIfYes() {
    return facilitiesIfYes;
  }

  public void setFacilitiesIfYes(List<DevukFacility> facilitiesIfYes) {
    this.facilitiesIfYes = facilitiesIfYes;
  }

  public List<DevukFacility> getFacilitiesIfPartially() {
    return facilitiesIfPartially;
  }

  public void setFacilitiesIfPartially(
      List<DevukFacility> facilitiesIfPartially) {
    this.facilitiesIfPartially = facilitiesIfPartially;
  }

  public Boolean getFacilitiesOffshore() {
    return facilitiesOffshore;
  }

  public void setFacilitiesOffshore(Boolean facilitiesOffshore) {
    this.facilitiesOffshore = facilitiesOffshore;
  }

  public Boolean getTransportsMaterialsToShore() {
    return transportsMaterialsToShore;
  }

  public void setTransportsMaterialsToShore(Boolean transportsMaterialsToShore) {
    this.transportsMaterialsToShore = transportsMaterialsToShore;
  }

  public String getTransportationMethod() {
    return transportationMethod;
  }

  public void setTransportationMethod(String transportationMethod) {
    this.transportationMethod = transportationMethod;
  }
}
