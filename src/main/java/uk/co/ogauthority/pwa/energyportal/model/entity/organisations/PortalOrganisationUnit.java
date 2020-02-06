package uk.co.ogauthority.pwa.energyportal.model.entity.organisations;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Immutable;

@Entity(name = "portal_organisation_units")
@Immutable
public class PortalOrganisationUnit {

  @Id
  private int ouId;

  private String name;

  @ManyToOne
  @JoinColumn(name = "org_grp_id")
  private PortalOrganisationGroup portalOrganisationGroup;

  public int getOuId() {
    return ouId;
  }

  public String getName() {
    return name;
  }

  public PortalOrganisationGroup getPortalOrganisationGroup() {
    return portalOrganisationGroup;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalOrganisationUnit)) {
      return false;
    }
    PortalOrganisationUnit that = (PortalOrganisationUnit) o;
    return ouId == that.ouId
        && name.equals(that.name)
        && Objects.equals(portalOrganisationGroup, that.portalOrganisationGroup);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ouId, name, portalOrganisationGroup);
  }
}
