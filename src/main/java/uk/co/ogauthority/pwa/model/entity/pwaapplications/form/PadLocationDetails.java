package uk.co.ogauthority.pwa.model.entity.pwaapplications.form;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_location_details")
public class PadLocationDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Column(name = "approximate_location")
  private String approximateProjectLocationFromShore;

  @Enumerated(EnumType.STRING)
  private HseSafetyZone withinSafetyZone;

  private Boolean facilitiesOffshore;
  private Boolean transportsMaterialsToShore;
  private String transportationMethod;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public String getApproximateProjectLocationFromShore() {
    return approximateProjectLocationFromShore;
  }

  public void setApproximateProjectLocationFromShore(String approximateLocationFromShore) {
    this.approximateProjectLocationFromShore = approximateLocationFromShore;
  }

  public HseSafetyZone getWithinSafetyZone() {
    return withinSafetyZone;
  }

  public void setWithinSafetyZone(HseSafetyZone withinSafetyZone) {
    this.withinSafetyZone = withinSafetyZone;
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
