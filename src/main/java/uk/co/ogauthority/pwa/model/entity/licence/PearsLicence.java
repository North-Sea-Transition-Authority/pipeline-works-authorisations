package uk.co.ogauthority.pwa.model.entity.licence;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.entity.converters.LicenceStatusConverter;
import uk.co.ogauthority.pwa.model.entity.enums.LicenceStatus;

@Immutable
@Entity
@Table(name = "ped_licences")
public class PearsLicence {

  @Id
  @Column(name = "plm_id", insertable = false, updatable = false)
  private Integer masterId;

  private String licenceType;
  private Integer licenceNumber;
  private String licenceName;

  @Convert(converter = LicenceStatusConverter.class)
  private LicenceStatus licenceStatus;

  public PearsLicence() {
  }

  @VisibleForTesting
  public PearsLicence(int masterId, String licenceType, int licenceNumber, String licenceName, LicenceStatus licenceStatus) {
    this.masterId = masterId;
    this.licenceType = licenceType;
    this.licenceNumber = licenceNumber;
    this.licenceName = licenceName;
    this.licenceStatus = licenceStatus;
  }

  public Integer getMasterId() {
    return masterId;
  }

  public void setMasterId(Integer masterId) {
    this.masterId = masterId;
  }

  public String getLicenceType() {
    return licenceType;
  }

  public void setLicenceType(String licenceType) {
    this.licenceType = licenceType;
  }

  public Integer getLicenceNumber() {
    return licenceNumber;
  }

  public void setLicenceNumber(Integer licenceNumber) {
    this.licenceNumber = licenceNumber;
  }

  public String getLicenceName() {
    return licenceName;
  }

  public void setLicenceName(String licenceName) {
    this.licenceName = licenceName;
  }

  public LicenceStatus getLicenceStatus() {
    return licenceStatus;
  }

  public void setLicenceStatus(LicenceStatus licenceStatus) {
    this.licenceStatus = licenceStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PearsLicence that = (PearsLicence) o;
    return Objects.equals(masterId, that.masterId)
        && Objects.equals(licenceType, that.licenceType)
        && Objects.equals(licenceNumber, that.licenceNumber)
        && Objects.equals(licenceName, that.licenceName)
        && licenceStatus == that.licenceStatus;
  }

  @Override
  public int hashCode() {
    return Objects.hash(masterId, licenceType, licenceNumber, licenceName, licenceStatus);
  }
}
