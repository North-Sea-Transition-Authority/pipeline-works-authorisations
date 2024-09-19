package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
