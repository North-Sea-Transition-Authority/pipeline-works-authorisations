package uk.co.ogauthority.pwa.service.teams;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;

@Service
public class PwaHolderTeamService {

  private final TeamService teamService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaHolderService pwaHolderService;

  @Autowired
  public PwaHolderTeamService(TeamService teamService,
                              PortalOrganisationsAccessor portalOrganisationsAccessor,
                              PwaHolderService pwaHolderService) {
    this.teamService = teamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaHolderService = pwaHolderService;
  }

  public boolean isPersonInHolderTeam(MasterPwa masterPwa, Person person) {

    var holderOrgGroups = pwaHolderService.getPwaHolderOrgGroups(masterPwa);

    return teamService
        .getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class))
        .stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .anyMatch(holderOrgGroups::contains);
  }

  // overload
  public boolean isPersonInHolderTeam(PwaApplicationDetail detail, Person person) {
    return isPersonInHolderTeam(detail.getMasterPwa(), person);
  }

  public boolean isPersonInHolderTeamWithRole(MasterPwa masterPwa, Person person, PwaOrganisationRole pwaOrganisationRole) {

    var holderOrgGroups = pwaHolderService.getPwaHolderOrgGroups(masterPwa);

    return teamService
        .getOrganisationTeamListIfPersonInRole(person, List.of(pwaOrganisationRole))
        .stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .anyMatch(holderOrgGroups::contains);
  }

  public List<PortalOrganisationUnit> getPortalOrganisationUnitsWhereUserHasOrgRole(WebUserAccount webUserAccount,
                                                                                    PwaOrganisationRole pwaOrganisationRole) {

    var organisationTeams = teamService.getOrganisationTeamListIfPersonInRole(
        webUserAccount.getLinkedPerson(), Set.of(pwaOrganisationRole));

    // all org units, including ended ones, if they still exist in the org grp.
    return portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(
        organisationTeams.stream()
            .map(PwaOrganisationTeam::getPortalOrganisationGroup)
            .collect(toList())
    );
  }

  public List<PortalOrganisationUnit> getPortalOrganisationUnitsWhereUserHasAnyOrgRole(WebUserAccount webUserAccount,
                                                                                       Set<PwaOrganisationRole> pwaOrganisationRoles) {

    var organisationTeams = teamService.getOrganisationTeamListIfPersonInRole(
        webUserAccount.getLinkedPerson(), pwaOrganisationRoles);

    // all org units, including ended ones, if they still exist in the org grp.
    return portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(
        organisationTeams.stream()
            .map(PwaOrganisationTeam::getPortalOrganisationGroup)
            .collect(toList())
    );
  }

  public List<PortalOrganisationGroup> getPortalOrganisationGroupsWhereUserHasOrgRole(WebUserAccount webUserAccount,
                                                                                     PwaOrganisationRole pwaOrganisationRole) {

    var organisationTeams = teamService.getOrganisationTeamListIfPersonInRole(
        webUserAccount.getLinkedPerson(), Set.of(pwaOrganisationRole));

    return organisationTeams.stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(toList());
  }

  /**
   * Return the roles that the person has in a holder team for any holder org, prioritising consented holder team if available,
   * falling back to detail holder team.
   */
  public Set<PwaOrganisationRole> getRolesInHolderTeam(PwaApplicationDetail detail, Person person) {

    var holderOrgGroups = pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa());

    var orgTeamsUserMemberOf = teamService.getOrganisationTeamsPersonIsMemberOf(person);

    var holderOrgTeamsUserMemberOf = orgTeamsUserMemberOf.stream()
        .filter(t -> holderOrgGroups.contains(t.getPortalOrganisationGroup()))
        .collect(toSet());

    // return roles that user has across all holder org teams
    return holderOrgTeamsUserMemberOf.stream()
        // repeated membership query per team unlikely to cause issues as normally 1 holder team per person
        .map(team -> teamService.getMembershipOfPersonInTeam(team, person))
        .flatMap(Optional::stream)
        .flatMap(teamMemberView -> teamMemberView.getRoleSet().stream())
        .map(roleView -> PwaOrganisationRole.resolveFromRoleName(roleView.getName()))
        .collect(toSet());

  }

  public Set<Person> getPeopleWithHolderTeamRole(PwaApplicationDetail detail,
                                                 PwaOrganisationRole role) {
    return getPeopleWithHolderTeamRoleForMasterPwa(detail.getMasterPwa(), role);
  }

  public Set<Person> getPeopleWithHolderTeamRoleForMasterPwa(MasterPwa masterPwa,
                                                 PwaOrganisationRole role) {
    var holderOrgGroups = pwaHolderService.getPwaHolderOrgGroups(masterPwa);
    return getPeopleWithHolderTeamRoleForOrgGroups(holderOrgGroups, role);
  }

  public Set<Person> getPeopleWithHolderTeamRoleForOrgGroup(PortalOrganisationGroup holderOrgGroup,
                                                            PwaOrganisationRole role) {
    return getPeopleWithHolderTeamRoleForOrgGroups(Set.of(holderOrgGroup), role);
  }

  private Set<Person> getPeopleWithHolderTeamRoleForOrgGroups(Set<PortalOrganisationGroup> holderOrgGroups,
                                                  PwaOrganisationRole role) {
    var orgTeams = teamService.getOrganisationTeamsForOrganisationGroups(holderOrgGroups);

    return orgTeams.stream()
        .flatMap(holderOrgTeam -> teamService.getTeamMembers(holderOrgTeam).stream())
        .filter(teamMember -> teamMember.getRoleSet().stream()
            .anyMatch(r -> r.getName().equals(role.getPortalTeamRoleName())))
        .map(PwaTeamMember::getPerson)
        .collect(toSet());

  }

  public Set<Person> getPersonsInHolderTeam(PwaApplicationDetail detail) {

    var holderOrgGroups = pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa());
    var orgTeams = teamService.getOrganisationTeamsForOrganisationGroups(holderOrgGroups);

    return orgTeams.stream()
        .flatMap(holderOrgTeam -> teamService.getTeamMembers(holderOrgTeam).stream())
        .map(PwaTeamMember::getPerson)
        .collect(toSet());
  }

}
