package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "portal_resources")
public class PortalTeam {

  @Id
  @Column(name = "res_id")
  private int resId;

  @ManyToOne
  @JoinColumn(name = "res_type", referencedColumnName = "res_type", insertable = false, updatable = false)
  private PortalTeamType portalTeamType;

  @Column(name = "res_name")
  private String name;

  private String description;

  public int getResId() {
    return resId;
  }

  public PortalTeamType getPortalTeamType() {
    return portalTeamType;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
