package uk.co.ogauthority.pwa.domain.energyportal.organisations.model;

import java.util.Objects;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PickableOrganisationUnitDetail;

/* Representing the core details of a current organisation unit */
public class OrganisationUnitDetailDto implements PickableOrganisationUnitDetail {

  private final OrganisationUnitId organisationUnitId;
  private final String registeredNumber;
  private final String companyName;
  private final String companyAddress;

  OrganisationUnitDetailDto(int organisationUnitId,
                                    String registeredNumber,
                                    String companyName,
                                    String companyAddress) {
    this.organisationUnitId = new OrganisationUnitId(organisationUnitId);
    this.registeredNumber = registeredNumber;
    this.companyName = companyName;
    this.companyAddress = companyAddress;
  }

  public static OrganisationUnitDetailDto from(PortalOrganisationUnitDetail portalOrganisationUnitDetail) {
    return new OrganisationUnitDetailDto(
        portalOrganisationUnitDetail.getOuId(),
        portalOrganisationUnitDetail.getRegisteredNumber(),
        portalOrganisationUnitDetail.getOrganisationUnitName(),
        portalOrganisationUnitDetail.getLegalAddress()
    );

  }

  public OrganisationUnitId getOrganisationUnitId() {
    return organisationUnitId;
  }

  @Override
  public int getOrgUnitId() {
    return this.organisationUnitId.asInt();
  }

  @Override
  public String getRegisteredNumber() {
    return registeredNumber;
  }

  @Override
  public String getCompanyName() {
    return companyName;
  }

  @Override
  public String getCompanyAddress() {
    return companyAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganisationUnitDetailDto that = (OrganisationUnitDetailDto) o;
    return organisationUnitId == that.organisationUnitId
        && Objects.equals(registeredNumber, that.registeredNumber)
        && Objects.equals(companyName, that.companyName)
        && Objects.equals(companyAddress, that.companyAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationUnitId, registeredNumber, companyName, companyAddress);
  }
}
