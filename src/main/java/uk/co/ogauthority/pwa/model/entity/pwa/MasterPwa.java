package uk.co.ogauthority.pwa.model.entity.pwa;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;

@Entity(name = "pwas")
public class MasterPwa {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "holder_ou_id")
  private PortalOrganisationUnit portalOrganisationUnit;

  private Instant createdTimestamp;

  public MasterPwa() {

  }

  public MasterPwa(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PortalOrganisationUnit getPortalOrganisationUnit() {
    return portalOrganisationUnit;
  }

  public void setPortalOrganisationUnit(
      PortalOrganisationUnit portalOrganisationUnit) {
    this.portalOrganisationUnit = portalOrganisationUnit;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }
}
