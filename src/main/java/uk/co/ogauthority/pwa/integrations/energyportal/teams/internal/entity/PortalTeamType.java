package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
