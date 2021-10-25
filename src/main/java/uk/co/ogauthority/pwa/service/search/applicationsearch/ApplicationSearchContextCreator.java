package uk.co.ogauthority.pwa.service.search.applicationsearch;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
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
  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  @Autowired
  public ApplicationSearchContextCreator(UserTypeService userTypeService,
                                         TeamService teamService,
                                         PortalOrganisationsAccessor portalOrganisationsAccessor,
                                         ConsulteeGroupTeamService consulteeGroupTeamService) {
    this.userTypeService = userTypeService;
    this.teamService = teamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }

  public ApplicationSearchContext createContext(AuthenticatedUserAccount authenticatedUserAccount) {
    var userTypes = userTypeService.getUserTypes(authenticatedUserAccount);
    var orgGroupsWhereMemberOfHolderTeam = teamService.getOrganisationTeamsPersonIsMemberOf(
        authenticatedUserAccount.getLinkedPerson()
    )
        .stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(toSet());

    // Do not limit by active orgs to ensure we can still access historic applications and PWA's.
    var orgUnitIdsAssociatedWithHolderTeamMembership = portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(
        orgGroupsWhereMemberOfHolderTeam)
        .stream()
        .map(OrganisationUnitId::from)
        .collect(toSet());

    var consulteeGroupIds = consulteeGroupTeamService.getTeamMemberByPerson(authenticatedUserAccount.getLinkedPerson())
        .map(consulteeGroupTeamMember -> ConsulteeGroupId.from(consulteeGroupTeamMember.getConsulteeGroup()))
        .map(Set::of)
        .orElse(Collections.emptySet());

    return new ApplicationSearchContext(
        authenticatedUserAccount,
        userTypes,
        orgGroupsWhereMemberOfHolderTeam,
        orgUnitIdsAssociatedWithHolderTeamMembership,
        consulteeGroupIds
    );
  }
}
