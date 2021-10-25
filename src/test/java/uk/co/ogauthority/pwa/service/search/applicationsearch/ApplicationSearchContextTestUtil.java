package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

public final class ApplicationSearchContextTestUtil {

  private ApplicationSearchContextTestUtil() {
    throw new UnsupportedOperationException("No object for you!");
  }

  public static ApplicationSearchContext emptyUserContext(AuthenticatedUserAccount authenticatedUserAccount,
                                                          UserType userType) {
    return new ApplicationSearchContext(
        authenticatedUserAccount,
        EnumSet.of(userType),
        Collections.emptySet(),
        Collections.emptySet(),
        Collections.emptySet());
  }

  public static ApplicationSearchContext industryAndOgaContext(AuthenticatedUserAccount authenticatedUserAccount,
                                                               Set<OrganisationUnitId> organisationUnitIdSet) {
    return new ApplicationSearchContext(
        authenticatedUserAccount,
        EnumSet.of(UserType.INDUSTRY, UserType.OGA),
        Collections.emptySet(),
        organisationUnitIdSet,
        Collections.emptySet());
  }

  public static ApplicationSearchContext industryContext(AuthenticatedUserAccount authenticatedUserAccount,
                                                         Set<OrganisationUnitId> organisationUnitIdSet) {
    return new ApplicationSearchContext(
        authenticatedUserAccount,
        EnumSet.of(UserType.INDUSTRY),
        Collections.emptySet(),
        organisationUnitIdSet,
        Collections.emptySet());
  }

  public static ApplicationSearchContext combinedIndustryOgaContext(AuthenticatedUserAccount authenticatedUserAccount,
                                                         Set<OrganisationUnitId> organisationUnitIdSet) {
    return new ApplicationSearchContext(
        authenticatedUserAccount,
        EnumSet.of(UserType.INDUSTRY, UserType.OGA),
        Collections.emptySet(),
        organisationUnitIdSet,
        Collections.emptySet());
  }

  public static ApplicationSearchContext consulteeContext(AuthenticatedUserAccount authenticatedUserAccount,
                                                         Set<ConsulteeGroupId> consulteeGroupIds) {
    return new ApplicationSearchContext(
        authenticatedUserAccount,
        EnumSet.of(UserType.CONSULTEE),
        Collections.emptySet(),
        Collections.emptySet(),
        consulteeGroupIds
    );
  }

}