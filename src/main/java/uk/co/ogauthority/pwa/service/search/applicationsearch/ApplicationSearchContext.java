package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

/**
 * Contains contextual information relevant to application search processing.
 */
public final class ApplicationSearchContext {
  private final AuthenticatedUserAccount authenticatedUserAccount;

  private final Set<UserType> userTypes;

  private final Set<PortalOrganisationGroup> orgGroupsWhereMemberOfHolderTeam;
  private final Set<OrganisationUnitId> orgUnitIdsAssociatedWithHolderTeamMembership;

  private final Set<ConsulteeGroupId> consulteeGroupIds;

  ApplicationSearchContext(AuthenticatedUserAccount authenticatedUserAccount,
                           Set<UserType> userTypes,
                           Set<PortalOrganisationGroup> orgGroupsWhereMemberOfHolderTeam,
                           Set<OrganisationUnitId> orgUnitIdsAssociatedWithHolderTeamMembership,
                           Set<ConsulteeGroupId> consulteeGroupIds) {
    this.authenticatedUserAccount = authenticatedUserAccount;
    this.userTypes = userTypes;
    this.orgGroupsWhereMemberOfHolderTeam = orgGroupsWhereMemberOfHolderTeam;
    this.orgUnitIdsAssociatedWithHolderTeamMembership = orgUnitIdsAssociatedWithHolderTeamMembership;
    this.consulteeGroupIds = consulteeGroupIds;
  }

  public AuthenticatedUserAccount getAuthenticatedUserAccount() {
    return authenticatedUserAccount;
  }

  public Set<UserType> getUserTypes() {
    return userTypes;
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

  public Set<ConsulteeGroupId> getConsulteeGroupIds() {
    return consulteeGroupIds;
  }


  public boolean containsSingleUserTypeOf(UserType userType) {
    return this.userTypes.size() == 1
        && this.userTypes.contains(userType);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationSearchContext that = (ApplicationSearchContext) o;
    return Objects.equals(authenticatedUserAccount, that.authenticatedUserAccount)
        && Objects.equals(userTypes, that.userTypes)
        && Objects.equals(orgGroupsWhereMemberOfHolderTeam, that.orgGroupsWhereMemberOfHolderTeam)
        && Objects.equals(orgUnitIdsAssociatedWithHolderTeamMembership,
        that.orgUnitIdsAssociatedWithHolderTeamMembership)
        && Objects.equals(consulteeGroupIds, that.consulteeGroupIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authenticatedUserAccount, userTypes, orgGroupsWhereMemberOfHolderTeam,
        orgUnitIdsAssociatedWithHolderTeamMembership, consulteeGroupIds);
  }
}
