package uk.co.ogauthority.pwa.service.teams;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.MasterPwaHolderDto;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;

@Service
public class PwaHolderTeamService {

  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final TeamService teamService;
  private final TeamManagementService teamManagementService;

  @Autowired
  public PwaHolderTeamService(PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
                              PadOrganisationRoleService padOrganisationRoleService,
                              TeamService teamService,
                              TeamManagementService teamManagementService) {
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.teamService = teamService;
    this.teamManagementService = teamManagementService;
  }

  /**
   * Return true if person is in holder team for any holder org, prioritising consented holder team if available,
   * falling back to detail holder team.
   */
  public boolean isPersonInHolderTeam(PwaApplicationDetail detail, Person person) {

    var holderOrgGroups = getHolderOrgGroups(detail);

    return teamService
        .getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .anyMatch(holderOrgGroups::contains);

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
        .collect(Collectors.toSet());

    // return roles that user has across all holder org teams
    return holderOrgTeamsUserMemberOf.stream()
        .map(team -> teamManagementService.getTeamMemberViewForTeamAndPerson(team, person))
        .flatMap(Optional::stream)
        .flatMap(teamMemberView -> teamMemberView.getRoleViews().stream())
        .map(roleView -> PwaOrganisationRole.resolveFromRoleName(roleView.getRoleName()))
        .collect(Collectors.toSet());

  }

  // TODO PWA-1148 - have dedicated service for this logic which is app type aware.
  public Set<PortalOrganisationGroup> getHolderOrgGroups(PwaApplicationDetail detail) {

    // first try and get the consented holders on the master pwa
    Set<PortalOrganisationGroup> holderOrgGroups = pwaConsentOrganisationRoleService
        .getCurrentHoldersOrgRolesForMasterPwa(detail.getPwaApplication().getMasterPwa())
        .stream()
        .map(MasterPwaHolderDto::getHolderOrganisationGroup)
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());

    // if there aren't any consented holders, then get the holders for the app detail we are looking at
    if (holderOrgGroups.isEmpty()) {
      holderOrgGroups = padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(detail, HuooRole.HOLDER).stream()
          .map(PadOrganisationRole::getOrganisationUnit)
          .map(PortalOrganisationUnit::getPortalOrganisationGroup)
          .collect(Collectors.toSet());
    }

    return holderOrgGroups;

  }

  public Set<Person> getPeopleWithHolderTeamRole(PwaApplicationDetail detail,
                                                 PwaOrganisationRole role) {

    var holderOrgGroups = getHolderOrgGroups(detail);

    var orgTeams = teamService.getAllOrganisationTeams();

    return orgTeams.stream()
        .filter(orgTeam -> holderOrgGroups.contains(orgTeam.getPortalOrganisationGroup()))
        .flatMap(holderOrgTeam -> teamService.getTeamMembers(holderOrgTeam).stream())
        .filter(teamMember -> teamMember.getRoleSet().stream()
          .anyMatch(r -> r.getName().equals(role.getPortalTeamRoleName())))
        .map(PwaTeamMember::getPerson)
        .collect(Collectors.toSet());

  }

}
