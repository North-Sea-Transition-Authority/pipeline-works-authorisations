package uk.co.ogauthority.pwa.service.teams;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.UserTeamRolesView;

@Service
public class PwaHolderTeamService {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaHolderService pwaHolderService;
  private final TeamQueryService teamQueryService;
  private final UserAccountService userAccountService;

  @Autowired
  public PwaHolderTeamService(PortalOrganisationsAccessor portalOrganisationsAccessor,
                              PwaHolderService pwaHolderService, TeamQueryService teamQueryService,
                              UserAccountService userAccountService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaHolderService = pwaHolderService;
    this.teamQueryService = teamQueryService;
    this.userAccountService = userAccountService;
  }

  public boolean isPersonInHolderTeam(MasterPwa masterPwa, WebUserAccount user) {

    var holderOrgGroups = pwaHolderService.getPwaHolderOrgGroups(masterPwa);
    var orgRoles = EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles());

    return holderOrgGroups.stream()
        .anyMatch(portalOrganisationGroup -> userIsInOrgTeamWithAnyRoleIn(user, portalOrganisationGroup, orgRoles));
  }

  private boolean userIsInOrgTeamWithAnyRoleIn(WebUserAccount user, PortalOrganisationGroup portalOrganisationGroup, Set<Role> roles) {
    var scopeRef = TeamScopeReference.from(portalOrganisationGroup.getOrgGrpId(), TeamType.ORGANISATION);

    return teamQueryService.userHasAtLeastOneScopedRole((long) user.getWuaId(), TeamType.ORGANISATION, scopeRef, roles);
  }

  public boolean isPersonInHolderTeamWithRole(MasterPwa masterPwa, WebUserAccount user, Role role) {

    var holderOrgGroups = pwaHolderService.getPwaHolderOrgGroups(masterPwa);

    return holderOrgGroups.stream()
        .anyMatch(portalOrganisationGroup -> userIsInOrgTeamWithAnyRoleIn(user, portalOrganisationGroup, Set.of(role)));
  }

  public List<PortalOrganisationUnit> getPortalOrganisationUnitsWhereUserHasAnyOrgRole(WebUserAccount webUserAccount,
                                                                                       Set<Role> roles) {

    List<PortalOrganisationGroup> portalOrganisationGroups = getPortalOrganisationGroupsWhereUserHasRoleIn(webUserAccount, roles);

    // all org units, including ended ones, if they still exist in the org grp.
    return portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(portalOrganisationGroups);
  }

  public List<PortalOrganisationGroup> getPortalOrganisationGroupsWhereUserHasRoleIn(WebUserAccount webUserAccount,
                                                                                     Set<Role> roles) {

    var orgGroupIdList = teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(webUserAccount.getWuaId(), TeamType.ORGANISATION, roles
        )
        .stream()
        .map(Team::getScopeId) // team scope id is the org group id
        .map(Integer::valueOf)
        .toList();

    return portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(orgGroupIdList);
  }

  /**
   * Return the roles that the person has in a holder team for any holder org, prioritising consented holder team if available,
   * falling back to detail holder team.
   */
  public Set<Role> getRolesInHolderTeam(PwaApplicationDetail detail, WebUserAccount user) {

    var holderOrgGroupIds = pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa()).stream()
        .map(PortalOrganisationGroup::getOrgGrpId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    return teamQueryService.getRolesForUserInScopedTeams(user.getWuaId(), TeamType.ORGANISATION, holderOrgGroupIds);
  }

  public Set<Person> getPeopleWithHolderTeamRole(PwaApplicationDetail pwaApplicationDetail,
                                                 Role role) {

    var holderOrgGroups = pwaHolderService.getPwaHolderOrgGroups(pwaApplicationDetail.getMasterPwa());
    return getPeopleWithHolderTeamRoleForOrgGroups(holderOrgGroups, role);
  }

  public Set<Person> getPeopleWithHolderTeamRoleForOrgGroup(PortalOrganisationGroup holderOrgGroup,
                                                            Role role) {
    return getPeopleWithHolderTeamRoleForOrgGroups(Set.of(holderOrgGroup), role);
  }

  private Set<Person> getPeopleWithHolderTeamRoleForOrgGroups(Set<PortalOrganisationGroup> holderOrgGroups,
                                                              Role role) {
    Set<String> holderOrgGroupIds = holderOrgGroups.stream()
        .map(PortalOrganisationGroup::getOrgGrpId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    var orgTeams = teamQueryService.getScopedTeamsByScopeIds(TeamType.ORGANISATION, holderOrgGroupIds);

    Set<Integer> wuaIdSet = orgTeams.stream()
        .flatMap(team -> teamQueryService.getUsersOfTeam(team).stream())
        .filter(teamMemberView -> teamMemberView.roles().contains(role))
        .map(UserTeamRolesView::wuaId)
        .map(Long::intValue)
        .collect(Collectors.toSet());

    return userAccountService.getPersonsByWuaIdSet(wuaIdSet);
  }

  public Set<Person> getPersonsInHolderTeam(PwaApplicationDetail detail) {
    // get the portal org group
    var holderOrgGroups = pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa());

    var wuaIdSet = holderOrgGroups.stream()
        .map(portalOrganisationGroup ->
            TeamScopeReference.from(portalOrganisationGroup.getOrgGrpId(), TeamType.ORGANISATION)
        )
        .flatMap(teamScopeReference -> teamQueryService.getUsersOfScopedTeam(TeamType.ORGANISATION, teamScopeReference).stream())
        .map(UserTeamRolesView::wuaId)
        .map(Long::intValue)
        .collect(Collectors.toSet());

    return userAccountService.getPersonsByWuaIdSet(wuaIdSet);
  }
}