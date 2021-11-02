package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
