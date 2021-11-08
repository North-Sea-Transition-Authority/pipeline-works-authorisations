package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "portal_res_members_current")
public class PortalTeamMember {

  @EmbeddedId
  private PortalTeamMemberId portalTeamMemberId;

  @Column(name = "person_id", insertable = false, updatable = false)
  private int personId;

  @ManyToOne
  @JoinColumn(name = "res_id", referencedColumnName = "res_id", insertable = false, updatable = false)
  private PortalTeam portalTeam;

  public int getPersonId() {
    return personId;
  }

  public PortalTeam getPortalTeam() {
    return portalTeam;
  }
}
