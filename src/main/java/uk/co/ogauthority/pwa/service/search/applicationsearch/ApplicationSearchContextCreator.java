package uk.co.ogauthority.pwa.service.search.applicationsearch;


import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.TeamType;

/**
 * Service to gather information relevant to application search processing.
 */
@Service
public class ApplicationSearchContextCreator {

  private final UserTypeService userTypeService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public ApplicationSearchContextCreator(UserTypeService userTypeService,
                                         PortalOrganisationsAccessor portalOrganisationsAccessor,
                                         ConsulteeGroupTeamService consulteeGroupTeamService,
                                         PwaHolderTeamService pwaHolderTeamService) {
    this.userTypeService = userTypeService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.pwaHolderTeamService = pwaHolderTeamService;
  }

  public ApplicationSearchContext createContext(AuthenticatedUserAccount authenticatedUserAccount) {
    var userTypes = userTypeService.getUserTypes(authenticatedUserAccount);
    var orgGroupsWhereMemberOfHolderTeam = pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(
        authenticatedUserAccount,
        EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles())
    );

    // Do not limit by active orgs to ensure we can still access historic applications and PWA's.
    var orgUnitIdsAssociatedWithHolderTeamMembership = portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(
        orgGroupsWhereMemberOfHolderTeam)
        .stream()
        .map(OrganisationUnitId::from)
        .collect(Collectors.toSet());

    var consulteeGroupIds = consulteeGroupTeamService.getTeamMemberByPerson(authenticatedUserAccount.getLinkedPerson())
        .map(consulteeGroupTeamMember -> ConsulteeGroupId.from(consulteeGroupTeamMember.getConsulteeGroup()))
        .map(Set::of)
        .orElse(Collections.emptySet());

    return new ApplicationSearchContext(
        authenticatedUserAccount,
        userTypes,
        new HashSet<>(orgGroupsWhereMemberOfHolderTeam),
        orgUnitIdsAssociatedWithHolderTeamMembership,
        consulteeGroupIds
    );
  }
}
