package uk.co.ogauthority.pwa.model.entity.devuk;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_safety_zone_structures")
public class PadFacility {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @ManyToOne
  @JoinColumn(name = "facility_id")
  private DevukFacility facility;

  @Column(name = "facility_name_manual_entry")
  private String facilityNameManualEntry;

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

  public DevukFacility getFacility() {
    return facility;
  }

  public void setFacility(DevukFacility facility) {
    this.facility = facility;
  }

  public String getFacilityNameManualEntry() {
    return facilityNameManualEntry;
  }

  public void setFacilityNameManualEntry(String fieldName) {
    this.facilityNameManualEntry = fieldName;
  }

  public boolean isLinkedToDevukFacility() {
    return facility != null;
  }
}
