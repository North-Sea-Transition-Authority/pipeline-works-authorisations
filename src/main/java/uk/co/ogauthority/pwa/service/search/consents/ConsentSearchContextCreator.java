package uk.co.ogauthority.pwa.service.search.consents;

import java.util.EnumSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class ConsentSearchContextCreator {

  private final UserTypeService userTypeService;
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public ConsentSearchContextCreator(UserTypeService userTypeService,
                                     PwaHolderTeamService pwaHolderTeamService) {
    this.userTypeService = userTypeService;
    this.pwaHolderTeamService = pwaHolderTeamService;
  }

  public ConsentSearchContext createContext(AuthenticatedUserAccount user) {

    var context = new ConsentSearchContext(user, userTypeService.getPriorityUserType(user));

    if (context.getUserType() == UserType.INDUSTRY) {

      var orgGroupsUserIsInTeamFor = pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(
              user,
              EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles())
          ).stream()
          .map(PortalOrganisationGroup::getOrgGrpId)
          .collect(Collectors.toSet());

      context.setOrgGroupIdsUserInTeamFor(orgGroupsUserIsInTeamFor);

    }

    return context;

  }

}
