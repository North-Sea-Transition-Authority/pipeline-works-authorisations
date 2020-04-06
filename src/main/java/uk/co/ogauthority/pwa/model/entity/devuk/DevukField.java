package uk.co.ogauthority.pwa.model.entity.devuk;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;

@Entity(name = "devuk_fields")
@Immutable
public class DevukField {

  @Id
  private Integer fieldId;
  private String fieldName;
  private Integer status;

  public DevukField() {
  }

  @VisibleForTesting
  public DevukField(int fieldId, String fieldName, int status) {
    this.fieldId = fieldId;
    this.fieldName = fieldName;
    this.status = status;
  }

  @ManyToOne
  @JoinColumn(name = "operator_ou_id")
  private PortalOrganisationUnit organisationUnit;

  public Integer getFieldId() {
    return fieldId;
  }

  public void setFieldId(Integer fieldId) {
    this.fieldId = fieldId;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public PortalOrganisationUnit getOrganisationUnit() {
    return organisationUnit;
  }

  public void setOrganisationUnit(
      PortalOrganisationUnit organisationUnit) {
    this.organisationUnit = organisationUnit;
  }
}
