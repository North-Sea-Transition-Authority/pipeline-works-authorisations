package uk.co.ogauthority.pwa.service.teams;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.repository.search.consents.PwaHolderOrgUnitRepository;

@Service
public class PwaHolderTeamService {

  private final TeamService teamService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaHolderOrgUnitRepository pwaHolderOrgUnitRepository;

  @Autowired
  public PwaHolderTeamService(TeamService teamService,
                              PortalOrganisationsAccessor portalOrganisationsAccessor,
                              PwaHolderOrgUnitRepository pwaHolderOrgUnitRepository) {
    this.teamService = teamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaHolderOrgUnitRepository = pwaHolderOrgUnitRepository;
  }

  public boolean isPersonInHolderTeam(MasterPwa masterPwa, Person person) {

    var holderOrgGroups = getHolderOrgGroups(masterPwa);

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

    var holderOrgGroups = getHolderOrgGroups(masterPwa);

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

    var holderOrgGroups = getHolderOrgGroups(detail);

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


  public Set<PortalOrganisationGroup> getHolderOrgGroups(MasterPwa masterPwa) {

    // the base view we are querying handles the logic of consented model lookup or application data lookup.
    // Uses the same logic as the app search so we are consistent across contexts.
    var holderOrgGrpIdsForMasterPwa = pwaHolderOrgUnitRepository.findAllByPwaId(masterPwa.getId())
        .stream()
        .map(PwaHolderOrgUnit::getOrgGrpId)
        .distinct()
        .collect(toList());

    return portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(holderOrgGrpIdsForMasterPwa)
        .stream()
        .collect(Collectors.toUnmodifiableSet());

  }

  public Set<PortalOrganisationGroup> getHolderOrgGroups(PwaApplicationDetail detail) {
    return getHolderOrgGroups(detail.getMasterPwa());
  }

  public Set<Person> getPeopleWithHolderTeamRole(PwaApplicationDetail detail,
                                                 PwaOrganisationRole role) {

    var holderOrgGroups = getHolderOrgGroups(detail);

    var orgTeams = teamService.getOrganisationTeamsForOrganisationGroups(holderOrgGroups);

    return orgTeams.stream()
        .flatMap(holderOrgTeam -> teamService.getTeamMembers(holderOrgTeam).stream())
        .filter(teamMember -> teamMember.getRoleSet().stream()
            .anyMatch(r -> r.getName().equals(role.getPortalTeamRoleName())))
        .map(PwaTeamMember::getPerson)
        .collect(toSet());

  }

  public Set<Person> getPersonsInHolderTeam(PwaApplicationDetail detail) {

    var holderOrgGroups = getHolderOrgGroups(detail);
    var orgTeams = teamService.getOrganisationTeamsForOrganisationGroups(holderOrgGroups);

    return orgTeams.stream()
        .flatMap(holderOrgTeam -> teamService.getTeamMembers(holderOrgTeam).stream())
        .map(PwaTeamMember::getPerson)
        .collect(toSet());
  }

}
