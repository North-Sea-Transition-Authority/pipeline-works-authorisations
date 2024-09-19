package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
