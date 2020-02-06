package uk.co.ogauthority.pwa.energyportal.model.entity.teams;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "portal_resource_type_roles")
public class PortalTeamTypeRole {

  @EmbeddedId
  private PortalTeamTypeRoleId portalTeamTypeRoleId;

  @ManyToOne
  @JoinColumn(name = "res_type", referencedColumnName = "res_type", updatable = false, insertable = false)
  private PortalTeamType portalTeamType;

  @Column(name = "role_name", insertable = false, updatable = false)
  private String name;

  @Column(name = "role_title")
  private String title;

  @Column(name = "role_description")
  private String description;

  private int minMems;

  private int maxMems;

  private int displaySeq;

  public PortalTeamTypeRoleId getPortalTeamTypeRoleId() {
    return portalTeamTypeRoleId;
  }

  public PortalTeamType getPortalTeamType() {
    return portalTeamType;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public int getMinMems() {
    return minMems;
  }

  public int getMaxMems() {
    return maxMems;
  }

  public int getDisplaySeq() {
    return displaySeq;
  }

  public String getName() {
    return name;
  }
}
