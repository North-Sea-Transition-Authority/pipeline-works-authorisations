package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "portal_resource_type_role_priv")
public class PortalTeamTypeRolePriv {

  @EmbeddedId
  private PortalTeamTypeRolePrivId portalTeamTypeRolePrivId;

  @ManyToOne
  @JoinColumns({
      @JoinColumn(name = "role_name", referencedColumnName = "role_name", insertable = false, updatable = false),
      @JoinColumn(name = "res_type", referencedColumnName = "res_type", insertable = false, updatable = false),
  })
  private PortalTeamTypeRole portalTeamTypeRole;

  // Maps entry in EmbeddedId so must be marked not updatable and not insertable
  @Column(name = "default_system_priv", insertable = false, updatable = false)
  private String privilege;

  public PortalTeamTypeRolePrivId getPortalTeamTypeRolePrivId() {
    return portalTeamTypeRolePrivId;
  }

  public PortalTeamTypeRole getPortalTeamTypeRole() {
    return portalTeamTypeRole;
  }

}
