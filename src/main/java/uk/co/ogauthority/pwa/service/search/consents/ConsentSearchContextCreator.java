package uk.co.ogauthority.pwa.service.search.consents;

import java.util.EnumSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@Service
public class ConsentSearchContextCreator {

  private final UserTypeService userTypeService;
  private final TeamService teamService;

  @Autowired
  public ConsentSearchContextCreator(UserTypeService userTypeService,
                                     TeamService teamService) {
    this.userTypeService = userTypeService;
    this.teamService = teamService;
  }

  public ConsentSearchContext createContext(AuthenticatedUserAccount user) {

    var context = new ConsentSearchContext(user, userTypeService.getPriorityUserType(user));

    if (context.getUserType() == UserType.INDUSTRY) {

      var orgGroupsUserIsInTeamFor = teamService
          .getOrganisationTeamListIfPersonInRole(user.getLinkedPerson(), EnumSet.allOf(PwaOrganisationRole.class))
          .stream()
          .map(PwaOrganisationTeam::getPortalOrganisationGroup)
          .map(PortalOrganisationGroup::getOrgGrpId)
          .collect(Collectors.toSet());

      context.setOrgGroupIdsUserInTeamFor(orgGroupsUserIsInTeamFor);

    }

    return context;

  }

}
