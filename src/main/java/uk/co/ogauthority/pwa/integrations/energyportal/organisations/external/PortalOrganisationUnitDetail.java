package uk.co.ogauthority.pwa.integrations.energyportal.organisations.external;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

  public String getOrganisationUnitName() {
    return this.organisationUnit.getName();
  }

}
