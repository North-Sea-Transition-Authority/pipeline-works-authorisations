package uk.co.ogauthority.pwa.energyportal.model.entity.teams;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "portal_resource_types")
public class PortalTeamType {

  @Id
  @Column(name = "res_type")
  private String type;

  @Column(name = "res_type_title")
  private String title;

  @Column(name = "res_type_description")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "scoped_within")
  private PortalTeamScopeType portalTeamScopeType;

  // TODO TEAMS_REFACTOR decide whether this working mapping from parent to children is required, or if child to parent only needed
  //  @OneToMany(fetch = FetchType.LAZY, mappedBy = "resType")
  //  private Set<PortalTeamTypeRole> portalTeamTypeRoles;


  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public PortalTeamScopeType getPortalTeamScopeType() {
    return portalTeamScopeType;
  }
}
