package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.Collections;
import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

public final class ApplicationSearchContextTestUtil {

  private ApplicationSearchContextTestUtil() {
    throw new RuntimeException("No object for you!");
  }

  public static ApplicationSearchContext emptyUserContext(AuthenticatedUserAccount authenticatedUserAccount,
                                                          UserType userType) {
    return new ApplicationSearchContext(
        authenticatedUserAccount,
        userType,
        Collections.emptySet(),
        Collections.emptySet()
    );
  }

  public static ApplicationSearchContext industryContext(AuthenticatedUserAccount authenticatedUserAccount,
                                                         Set<OrganisationUnitId> organisationUnitIdSet) {
    return new ApplicationSearchContext(
        authenticatedUserAccount,
        UserType.INDUSTRY,
        Collections.emptySet(),
        organisationUnitIdSet
    );
  }

}