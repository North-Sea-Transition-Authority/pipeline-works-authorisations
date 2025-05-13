package uk.co.ogauthority.pwa.service.search.applicationsearch;


import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

/**
 * Service to gather information relevant to application search processing.
 */
@Service
public class ApplicationSearchContextCreator {

  private final UserTypeService userTypeService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public ApplicationSearchContextCreator(UserTypeService userTypeService,
                                         PortalOrganisationsAccessor portalOrganisationsAccessor,
                                         PwaHolderTeamService pwaHolderTeamService,
                                         TeamQueryService teamQueryService) {
    this.userTypeService = userTypeService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.teamQueryService = teamQueryService;
  }

  public ApplicationSearchContext createContext(AuthenticatedUserAccount authenticatedUserAccount) {
    var userTypes = userTypeService.getUserTypes(authenticatedUserAccount);
    var orgGroupsWhereMemberOfHolderTeam = pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(
        authenticatedUserAccount,
        TeamType.ORGANISATION.getAllowedRolesAsSet()
    );

    // Do not limit by active orgs to ensure we can still access historic applications and PWA's.
    var orgUnitIdsAssociatedWithHolderTeamMembership = portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(
        orgGroupsWhereMemberOfHolderTeam)
        .stream()
        .map(OrganisationUnitId::from)
        .collect(Collectors.toSet());

    var consulteeGroupIds = teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(
            authenticatedUserAccount.getWuaId(),
            TeamType.CONSULTEE,
            TeamType.CONSULTEE.getAllowedRolesAsSet()
        )
        .stream()
        .map(team -> ConsulteeGroupId.from(Integer.valueOf(team.getScopeId())))
        .collect(Collectors.toSet());

    return new ApplicationSearchContext(
        authenticatedUserAccount,
        userTypes,
        new HashSet<>(orgGroupsWhereMemberOfHolderTeam),
        orgUnitIdsAssociatedWithHolderTeamMembership,
        consulteeGroupIds
    );
  }
}
