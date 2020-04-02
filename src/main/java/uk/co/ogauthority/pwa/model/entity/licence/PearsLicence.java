package uk.co.ogauthority.pwa.model.entity.licence;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity(name = "ped_licences")
public class PearsLicence {

  @Id
  @Column(name = "plm_id", insertable = false, updatable = false)
  private Integer masterId;

  private String licenceType;
  private Integer licenceNumber;
  private String licenceName;

  public PearsLicence() {
  }

  @VisibleForTesting
  public PearsLicence(int masterId, String licenceType, int licenceNumber, String licenceName) {
    this.masterId = masterId;
    this.licenceType = licenceType;
    this.licenceNumber = licenceNumber;
    this.licenceName = licenceName;
  }

  public int getMasterId() {
    return masterId;
  }

  public void setMasterId(int id) {
    this.masterId = id;
  }

  public String getLicenceType() {
    return licenceType;
  }

  public void setLicenceType(String licenceType) {
    this.licenceType = licenceType;
  }

  public int getLicenceNumber() {
    return licenceNumber;
  }

  public void setLicenceNumber(int licenceNumber) {
    this.licenceNumber = licenceNumber;
  }

  public String getLicenceName() {
    return licenceName;
  }

  public void setLicenceName(String licenceName) {
    this.licenceName = licenceName;
  }
}
