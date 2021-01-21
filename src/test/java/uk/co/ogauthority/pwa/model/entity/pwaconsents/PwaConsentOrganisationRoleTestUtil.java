package uk.co.ogauthority.pwa.model.entity.pwaconsents;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

public class PwaConsentOrganisationRoleTestUtil {

  private PwaConsentOrganisationRoleTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
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


}