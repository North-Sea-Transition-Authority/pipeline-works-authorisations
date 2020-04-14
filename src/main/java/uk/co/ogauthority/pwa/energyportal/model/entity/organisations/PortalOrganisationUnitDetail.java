package uk.co.ogauthority.pwa.energyportal.model.entity.organisations;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Immutable;

@Entity(name = "portal_org_unit_detail")
@Immutable
public class PortalOrganisationUnitDetail {

  @Id
  @Column(name = "ou_id")
  private Integer ouId;

  @JoinColumn(name = "org_unit_id")
  @ManyToOne
  private PortalOrganisationUnit organisationUnit;

  private String legalAddress;
  private String registeredNumber;

  public Integer getOuId() {
    return ouId;
  }

  public PortalOrganisationUnit getOrganisationUnit() {
    return organisationUnit;
  }

  public void setOrganisationUnit(
      PortalOrganisationUnit organisationUnit) {
    this.organisationUnit = organisationUnit;
  }

  public String getLegalAddress() {
    return legalAddress;
  }

  public String getRegisteredNumber() {
    return registeredNumber;
  }

}
