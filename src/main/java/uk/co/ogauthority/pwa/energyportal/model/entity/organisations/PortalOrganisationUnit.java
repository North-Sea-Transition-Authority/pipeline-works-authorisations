package uk.co.ogauthority.pwa.energyportal.model.entity.organisations;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;

@Entity
@Table(name = "portal_organisation_units")
@Immutable
public class PortalOrganisationUnit implements SearchSelectable {

  @Id
  private int ouId;

  private String name;

  @ManyToOne
  @JoinColumn(name = "org_grp_id")
  private PortalOrganisationGroup portalOrganisationGroup;

  public PortalOrganisationUnit() {
  }

  @VisibleForTesting
  public PortalOrganisationUnit(int ouId, String name) {
    this.ouId = ouId;
    this.name = name;
  }

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

  @Override
  public String getSelectionId() {
    return String.valueOf(ouId);
  }

  @Override
  public String getSelectionText() {
    return name;
  }
}
