package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

/**
 * Contains contextual information the relevant to application search processing.
 */
public class ApplicationSearchContext {
  private final AuthenticatedUserAccount authenticatedUserAccount;

  private final UserType userType;

  private final Set<PortalOrganisationGroup> orgGroupsWhereMemberOfHolderTeam;
  private final Set<OrganisationUnitId> orgUnitIdsAssociatedWithHolderTeamMembership;

  ApplicationSearchContext(AuthenticatedUserAccount authenticatedUserAccount,
                           UserType userType,
                           Set<PortalOrganisationGroup> orgGroupsWhereMemberOfHolderTeam,
                           Set<OrganisationUnitId> orgUnitIdsAssociatedWithHolderTeamMembership) {
    this.authenticatedUserAccount = authenticatedUserAccount;
    this.userType = userType;
    this.orgGroupsWhereMemberOfHolderTeam = orgGroupsWhereMemberOfHolderTeam;
    this.orgUnitIdsAssociatedWithHolderTeamMembership = orgUnitIdsAssociatedWithHolderTeamMembership;
  }

  public AuthenticatedUserAccount getAuthenticatedUserAccount() {
    return authenticatedUserAccount;
  }

  public UserType getUserType() {
    return userType;
  }

  public int getWuaIdAsInt() {
    return authenticatedUserAccount.getWuaId();
  }

  public Set<PortalOrganisationGroup> getOrgGroupsWhereMemberOfHolderTeam() {
    return orgGroupsWhereMemberOfHolderTeam;
  }

  public Set<OrganisationUnitId> getOrgUnitIdsAssociatedWithHolderTeamMembership() {
    return orgUnitIdsAssociatedWithHolderTeamMembership;
  }
}
