package uk.co.ogauthority.pwa.model.dto.consents;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

/**
 * Class which captures an "organisation" role owner. This is the organisation or treaty which owns a particular role instance for a PWA.
 * A role owner is not a instance of a HuooRole, but the concept(org unit or treaty) that could have an instance of the HUOO roles.
 */
public final class OrganisationRoleOwnerDto {

  private final HuooType huooType;
  private final OrganisationUnitId organisationUnitId;
  private final String manualOrganisationName;
  private final TreatyAgreement treatyAgreement;

  public OrganisationRoleOwnerDto(HuooType huooType,
                                  OrganisationUnitId organisationUnitId,
                                  String manualOrganisationName,
                                  TreatyAgreement treatyAgreement) {
    this.huooType = huooType;
    this.organisationUnitId = organisationUnitId;
    this.manualOrganisationName = manualOrganisationName;
    this.treatyAgreement = treatyAgreement;
  }

  public HuooType getHuooType() {
    return huooType;
  }

  public OrganisationUnitId getOrganisationUnitId() {
    return organisationUnitId;
  }

  public String getManualOrganisationName() {
    return manualOrganisationName;
  }

  public TreatyAgreement getTreatyAgreement() {
    return treatyAgreement;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganisationRoleOwnerDto that = (OrganisationRoleOwnerDto) o;
    return huooType == that.huooType
        && Objects.equals(organisationUnitId, that.organisationUnitId)
        && Objects.equals(manualOrganisationName, that.manualOrganisationName)
        && treatyAgreement == that.treatyAgreement;
  }

  @Override
  public int hashCode() {
    return Objects.hash(huooType, organisationUnitId, manualOrganisationName, treatyAgreement);
  }
}
