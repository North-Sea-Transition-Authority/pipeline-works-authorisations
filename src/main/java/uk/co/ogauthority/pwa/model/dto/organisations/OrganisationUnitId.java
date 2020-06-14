package uk.co.ogauthority.pwa.model.dto.organisations;

import java.util.Objects;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;

/* Wraps the data level unique identifier for an organisation unit to prevent mistakes where primitive data type ids are passed around.*/
public class OrganisationUnitId {

  private final int id;

  public OrganisationUnitId(int id) {
    this.id = id;
  }

  public static OrganisationUnitId from(PortalOrganisationUnit portalOrganisationUnit) {
    return new OrganisationUnitId(portalOrganisationUnit.getOuId());
  }

  public int asInt() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganisationUnitId that = (OrganisationUnitId) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
