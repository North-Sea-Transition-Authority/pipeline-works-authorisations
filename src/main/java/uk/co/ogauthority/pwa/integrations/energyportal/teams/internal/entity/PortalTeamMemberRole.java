package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "portal_res_memb_current_roles")
public class PortalTeamMemberRole {

  @EmbeddedId
  private PortalTeamMemberRoleId portalTeamMemberRoleId;

  @ManyToOne
  @JoinColumns({
      @JoinColumn(name = "person_id", referencedColumnName = "person_id", insertable = false, updatable = false),
      @JoinColumn(name = "res_id", referencedColumnName = "res_id", insertable = false, updatable = false)
  })
  private PortalTeamMember portalTeamMember;

  @ManyToOne
  @JoinColumns({
      @JoinColumn(name = "res_type", referencedColumnName = "res_type", insertable = false, updatable = false),
      @JoinColumn(name = "role_name", referencedColumnName = "role_name", insertable = false, updatable = false)
  })
  private PortalTeamTypeRole portalTeamTypeRole;

  public PortalTeamMemberRoleId getPortalTeamMemberRoleId() {
    return portalTeamMemberRoleId;
  }

  public PortalTeamTypeRole getPortalTeamTypeRole() {
    return portalTeamTypeRole;
  }
}
