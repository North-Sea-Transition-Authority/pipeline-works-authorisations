package uk.co.ogauthority.pwa.service.orgs;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

/**
 * Wrapper class for PortalOrganisationsAccessor to retrieve organisations that a user has permission to access.
 */
@Service
public class PwaOrganisationAccessor {

  private final TeamService teamService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final UserTypeService userTypeService;

  @Autowired
  public PwaOrganisationAccessor(TeamService teamService,
                                 PortalOrganisationsAccessor portalOrganisationsAccessor,
                                 UserTypeService userTypeService) {
    this.teamService = teamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.userTypeService = userTypeService;
  }

  public List<PortalOrganisationGroup> getOrgGroupsUserCanAccess(AuthenticatedUserAccount user) {

    if (userTypeService.getPriorityUserType(user) == UserType.OGA) {
      return portalOrganisationsAccessor.getAllOrganisationGroups();
    }

    return teamService.getOrganisationTeamListIfPersonInRole(user.getLinkedPerson(), List.of(PwaOrganisationRole.APPLICATION_CREATOR))
        .stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(Collectors.toList());
  }

  public List<PortalOrganisationUnit> getOrgUnitsUserCanAccess(AuthenticatedUserAccount user) {

    if (userTypeService.getPriorityUserType(user) == UserType.OGA) {
      return portalOrganisationsAccessor.getAllOrganisationUnits();
    }

    return portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(getOrgGroupsUserCanAccess(user));

  }

}
