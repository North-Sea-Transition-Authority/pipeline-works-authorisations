package uk.co.ogauthority.pwa.service.pwaconsents;

import java.util.Optional;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

public class MasterPwaHolderDto {

  private static final HuooRole HUOO_ROLE = HuooRole.HOLDER;
  private static final HuooType HUOO_TYPE = HuooType.PORTAL_ORG;

  private final PortalOrganisationUnit organisationUnit;

  private final PwaConsent addedByPwaConsent;

  public MasterPwaHolderDto(
      PortalOrganisationUnit organisationUnit,
      PwaConsent addedByPwaConsent) {
    this.organisationUnit = organisationUnit;
    this.addedByPwaConsent = addedByPwaConsent;
  }

  public MasterPwa getMasterPwa() {
    return addedByPwaConsent.getMasterPwa();
  }

  public Optional<PortalOrganisationGroup> getHolderOrganisationGroup() {
    return this.organisationUnit.getPortalOrganisationGroup();
  }

  public Optional<PortalOrganisationUnit> getHolderOrganisationUnit() {
    return Optional.ofNullable(this.organisationUnit);
  }

}
