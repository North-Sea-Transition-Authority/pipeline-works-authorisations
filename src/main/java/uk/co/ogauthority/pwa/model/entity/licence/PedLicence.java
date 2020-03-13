package uk.co.ogauthority.pwa.model.entity.licence;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity(name = "ped_licences")
public class PedLicence {

  @Id
  private int id;

  private String licenceType;
  private int licenceNumber;
  private String licenceName;

  public PedLicence() {
  }

  @VisibleForTesting
  public PedLicence(int id, String licenceType, int licenceNumber, String licenceName) {
    this.id = id;
    this.licenceType = licenceType;
    this.licenceNumber = licenceNumber;
    this.licenceName = licenceName;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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
