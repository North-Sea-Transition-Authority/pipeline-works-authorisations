package uk.co.ogauthority.pwa.energyportal.model.entity.organisations;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;


@Entity
@Table(name = "portal_organisation_groups")
@Immutable
public class PortalOrganisationGroup {

  public static final String UREF_TYPE = "++REGOGRGRP";

  @Id
  private Integer orgGrpId;

  private String name;

  private String shortName;

  private String urefValue;

  public Integer getOrgGrpId() {
    return orgGrpId;
  }

  public String getName() {
    return name;
  }

  public String getShortName() {
    return shortName;
  }

  public String getUrefValue() {
    return urefValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalOrganisationGroup)) {
      return false;
    }
    PortalOrganisationGroup that = (PortalOrganisationGroup) o;
    return orgGrpId.equals(that.orgGrpId)
        && name.equals(that.name)
        && shortName.equals(that.shortName)
        && urefValue.equals(that.urefValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orgGrpId, name, shortName, urefValue);
  }
}
