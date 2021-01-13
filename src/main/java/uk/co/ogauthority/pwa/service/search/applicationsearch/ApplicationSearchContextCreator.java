package uk.co.ogauthority.pwa.service.search.applicationsearch;

import static java.util.stream.Collectors.toSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

/**
 * Service to gather information relevant to application search processing.
 */
@Service
public class ApplicationSearchContextCreator {

  private final UserTypeService userTypeService;
  private final TeamService teamService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public ApplicationSearchContextCreator(UserTypeService userTypeService,
                                         TeamService teamService,
                                         PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.userTypeService = userTypeService;
    this.teamService = teamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  public ApplicationSearchContext createContext(AuthenticatedUserAccount authenticatedUserAccount) {
    var primaryUserType = userTypeService.getUserType(authenticatedUserAccount);
    var orgGroupsWhereMemberOfHolderTeam = teamService.getOrganisationTeamsPersonIsMemberOf(
        authenticatedUserAccount.getLinkedPerson()
    )
        .stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(toSet());

    var orgUnitIdsAssociatedWithHolderTeamMembership = portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(
        orgGroupsWhereMemberOfHolderTeam)
        .stream()
        .map(OrganisationUnitId::from)
        .collect(toSet());

    return new ApplicationSearchContext(
        authenticatedUserAccount,
        primaryUserType,
        orgGroupsWhereMemberOfHolderTeam,
        orgUnitIdsAssociatedWithHolderTeamMembership);
  }
}
