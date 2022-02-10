package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "portal_resource_usages_current")
public class PortalTeamUsage {

  @EmbeddedId
  private PortalTeamUsageId portalTeamUsageId;

  @ManyToOne
  @JoinColumn(name = "res_id", referencedColumnName = "res_id", updatable = false, insertable = false)
  private PortalTeam portalTeam;

  // Maps entry in EmbeddedId so must be marked not updatable and not insertable
  @Enumerated(EnumType.STRING)
  @Column(insertable = false, updatable = false)
  private PortalTeamUsagePurpose purpose;

  @Column(name = "uref", insertable = false, updatable = false)
  private String uref;

  public PortalTeamUsageId getPortalTeamUsageId() {
    return portalTeamUsageId;
  }

  public PortalTeamUsagePurpose getPurpose() {
    return purpose;
  }

  public String getUref() {
    return this.portalTeamUsageId.getUref();
  }

  public PortalTeam getPortalTeam() {
    return portalTeam;
  }
}
