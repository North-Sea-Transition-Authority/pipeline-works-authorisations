package uk.co.ogauthority.pwa.model.dto.consents;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Optional;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

/**
 * Data class capturing an instance of an organisation's role for some PWA.
 * Use Cases:
 * a) building block object for creating application huoo data from consent model
 * b) building block object for diffing application huoo data from consent model
 */
public final class OrganisationRoleInstanceDto {

  private final OrganisationRoleOwnerDto organisationRoleOwnerDto;
  private final HuooRole huooRole;

  public OrganisationRoleInstanceDto(Integer organisationUnitId,
                                     String manualOrganisationName,
                                     TreatyAgreement treatyAgreement,
                                     HuooRole huooRole,
                                     HuooType huooType) {

    this.organisationRoleOwnerDto = new OrganisationRoleOwnerDto(
        huooType,
        organisationUnitId != null ? new OrganisationUnitId(organisationUnitId) : null,
        manualOrganisationName,
        treatyAgreement
    );

    this.huooRole = huooRole;

  }


  @VisibleForTesting
  public OrganisationRoleInstanceDto(PadOrganisationRole padOrganisationRole) {
    this(padOrganisationRole.getOrganisationUnit().getOuId(),
        null,
        null,
        padOrganisationRole.getRole(),
        padOrganisationRole.getType());
  }


  public OrganisationRoleOwnerDto getOrganisationRoleOwnerDto() {
    return organisationRoleOwnerDto;
  }

  // TODO PWA-637 this is probably not going to be needed whe treaties are fully supported
  public OrganisationUnitId getOrganisationUnitId() {
    return this.organisationRoleOwnerDto.getOrganisationUnitId();
  }

  // TODO PWA-637 this is probably not going to be needed whe treaties are fully supported
  public boolean isPortalOrgRole() {
    return this.organisationRoleOwnerDto.getOrganisationUnitId() != null;
  }

  // TODO PWA-637 this is probably not going to be needed whe treaties are fully supported
  public Optional<String> getManualOrganisationName() {
    return Optional.ofNullable(this.organisationRoleOwnerDto.getManualOrganisationName());
  }

  public HuooRole getHuooRole() {
    return huooRole;
  }

  public HuooType getHuooType() {
    return this.organisationRoleOwnerDto.getHuooType();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganisationRoleInstanceDto that = (OrganisationRoleInstanceDto) o;
    return Objects.equals(organisationRoleOwnerDto, that.organisationRoleOwnerDto)
        && huooRole == that.huooRole;
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleOwnerDto, huooRole);
  }
}
