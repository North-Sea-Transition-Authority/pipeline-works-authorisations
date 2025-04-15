package uk.co.ogauthority.pwa.service.orgs;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationSearchUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

/**
 * Wrapper class for PortalOrganisationsAccessor to retrieve organisations that a user has permission to access.
 */
@Service
public class PwaOrganisationAccessor {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final UserTypeService userTypeService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public PwaOrganisationAccessor(PortalOrganisationsAccessor portalOrganisationsAccessor,
                                 UserTypeService userTypeService,
                                 TeamQueryService teamQueryService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.userTypeService = userTypeService;
    this.teamQueryService = teamQueryService;
  }

  public List<PortalOrganisationGroup> getOrgGroupsUserCanAccess(AuthenticatedUserAccount user) {

    if (userTypeService.getPriorityUserTypeOrThrow(user) == UserType.OGA) {
      return portalOrganisationsAccessor.getAllOrganisationGroups();
    }

    var orgGroupIdList = teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(
            user.getWuaId(),
            TeamType.ORGANISATION,
            List.of(Role.APPLICATION_CREATOR)
        )
        .stream()
        .map(Team::getScopeId)
        .map(Integer::valueOf)
        .toList();

    return portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(orgGroupIdList);
  }

  public List<PortalOrganisationGroup> findOrganisationGroupsWhereNameContains(String searchTerm) {
    return portalOrganisationsAccessor.getAllOrganisationGroupsWhereNameContains(searchTerm);
  }

  public PortalOrganisationGroup getOrganisationGroupOrError(Integer orgGrpId) {
    return portalOrganisationsAccessor.getOrganisationGroupById(orgGrpId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Unable to find organisation group with id %d", orgGrpId))
        );
  }

  public List<PortalOrganisationSearchUnit> getOrgUnitsUserCanAccess(AuthenticatedUserAccount user) {

    if (userTypeService.getPriorityUserTypeOrThrow(user) == UserType.OGA) {
      return portalOrganisationsAccessor.getAllActiveOrganisationUnitsSearch();
    }

    var orgGroups = getOrgGroupsUserCanAccess(user);

    return portalOrganisationsAccessor.getSearchableOrganisationUnitsForOrganisationGroupsIn(orgGroups);
  }

}
