package uk.co.ogauthority.pwa.model.entity.pwaconsents;

import java.time.Instant;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

public class PwaConsentOrganisationRoleTestUtil {

  private PwaConsentOrganisationRoleTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }


  public static PwaConsentOrganisationRole createMigratedOrgRole(PwaConsent pwaConsent,
                                                                  String migratedOrgRoleName,
                                                                  HuooRole huooRole){

    var orgRole = new PwaConsentOrganisationRole();
    orgRole.setAddedByPwaConsent(pwaConsent);
    orgRole.setMigratedOrganisationName(migratedOrgRoleName);
    orgRole.setRole(huooRole);
    orgRole.setType(HuooType.PORTAL_ORG);
    orgRole.setStartTimestamp(Instant.now());
    return orgRole;

  }

  public static PwaConsentOrganisationRole createOrganisationRole(PwaConsent pwaConsent,
                                                                  OrganisationUnitId organisationUnitId,
                                                                  HuooRole huooRole){

    var orgRole = new PwaConsentOrganisationRole();
    orgRole.setAddedByPwaConsent(pwaConsent);
    orgRole.setOrganisationUnitId(organisationUnitId.asInt());
    orgRole.setRole(huooRole);
    orgRole.setType(HuooType.PORTAL_ORG);
    orgRole.setStartTimestamp(Instant.now());
    return orgRole;

  }

  public static PwaConsentOrganisationRole createTreatyRole(PwaConsent pwaConsent,
                                                            TreatyAgreement treatyAgreement,
                                                            HuooRole huooRole){

    var orgRole = new PwaConsentOrganisationRole();
    orgRole.setAddedByPwaConsent(pwaConsent);
    orgRole.setAgreement(treatyAgreement);
    orgRole.setRole(huooRole);
    orgRole.setType(HuooType.TREATY_AGREEMENT);
    orgRole.setStartTimestamp(Instant.now());
    return orgRole;

  }


}