package uk.co.ogauthority.pwa.model.dto.consents;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Optional;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

/**
 * Data class capturing an instance of an organisation's role for some PWA.
 * Use Cases:
 * a) building block object for creating application huoo data from consent model
 * b) building block object for diffing application huoo data from consent model
 */
public class OrganisationRoleDto {

  private final OrganisationUnitId organisationUnitId;
  private final String manualOrganisationName;
  private final HuooRole huooRole;
  private final HuooType huooType;

  public OrganisationRoleDto(Integer organisationUnitId,
                             String manualOrganisationName,
                             HuooRole huooRole,
                             HuooType huooType) {
    this.organisationUnitId = organisationUnitId != null ? new OrganisationUnitId(organisationUnitId) : null;
    this.manualOrganisationName = manualOrganisationName;
    this.huooRole = huooRole;
    this.huooType = huooType;
  }


  @VisibleForTesting
  public OrganisationRoleDto(PadOrganisationRole padOrganisationRole) {
    this(padOrganisationRole.getOrganisationUnit().getOuId(),
        null,
        padOrganisationRole.getRole(),
        padOrganisationRole.getType());
  }


  public OrganisationUnitId getOrganisationUnitId() {
    return organisationUnitId;
  }

  public boolean isPortalOrgRole() {
    return organisationUnitId != null;
  }

  public Optional<String> getManualOrganisationName() {
    return Optional.ofNullable(this.manualOrganisationName);
  }

  public HuooRole getHuooRole() {
    return huooRole;
  }

  public HuooType getHuooType() {
    return huooType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganisationRoleDto that = (OrganisationRoleDto) o;
    return Objects.equals(organisationUnitId,
        that.organisationUnitId)
        && huooRole == that.huooRole
        && huooType == that.huooType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationUnitId, manualOrganisationName, huooRole, huooType);
  }
}
