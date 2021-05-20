package uk.co.ogauthority.pwa.service.teams;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.service.teams.PortalTeamAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;

@Service
public class TeamService {

  private final PortalTeamAccessor portalTeamAccessor;
  private final PwaTeamsDtoFactory pwaTeamsDtoFactory;
  private final PwaUserPrivilegeService pwaUserPrivilegeService;

  @Autowired
  public TeamService(PortalTeamAccessor portalTeamAccessor,
                     PwaTeamsDtoFactory pwaTeamsDtoFactory,
                     PwaUserPrivilegeService pwaUserPrivilegeService) {
    this.portalTeamAccessor = portalTeamAccessor;
    this.pwaTeamsDtoFactory = pwaTeamsDtoFactory;
    this.pwaUserPrivilegeService = pwaUserPrivilegeService;
  }

  /**
   * Return the PWA Regulator team.
   */
  public PwaRegulatorTeam getRegulatorTeam() {
    List<PortalTeamDto> teamList = portalTeamAccessor.getPortalTeamsByPortalTeamType(
        PwaTeamType.REGULATOR.getPortalTeamType()
    );
    return createRegulatorTeamOrError(teamList);
  }

  /**
   * Return all organisation teams.
   */
  public List<PwaOrganisationTeam> getAllOrganisationTeams() {
    List<PortalTeamDto> orgTeams = portalTeamAccessor.getPortalTeamsByPortalTeamType(
        PwaTeamType.ORGANISATION.getPortalTeamType()
    );
    return pwaTeamsDtoFactory.createOrganisationTeamList(orgTeams);
  }

  /**
   * Return all organisation teams associated with org groups.
   */
  public List<PwaOrganisationTeam> getOrganisationTeamsForOrganisationGroups(
      Collection<PortalOrganisationGroup> organisationGroups) {

    var orgGroupUrefs = organisationGroups
        .stream()
        .map(PortalOrganisationGroup::getUrefValue).collect(Collectors.toSet());
    List<PortalTeamDto> orgTeams = portalTeamAccessor.getPortalTeamsByPortalTeamType(PwaTeamType.ORGANISATION.getPortalTeamType())
        .stream()
        .filter(portalTeamDto -> orgGroupUrefs.contains(portalTeamDto.getScope().getPrimaryScope()))
        .collect(Collectors.toUnmodifiableList());

    return pwaTeamsDtoFactory.createOrganisationTeamList(orgTeams);
  }

  /**
   * Return the PwaTeam with the given resId.
   */
  public PwaTeam getTeamByResId(int resId) {
    return portalTeamAccessor.findPortalTeamById(resId)
        .map(pwaTeamsDtoFactory::createPwaTeam)
        .orElseThrow(() -> new PwaEntityNotFoundException("PwaTeam not found for resId: " + resId));
  }

  /**
   * Return all members of the given team.
   */
  public List<PwaTeamMember> getTeamMembers(PwaTeam team) {
    return pwaTeamsDtoFactory.createPwaTeamMemberList(portalTeamAccessor.getPortalTeamMembers(team.getId()), team);
  }

  /**
   * For a given person and team get the role membership of that person. If not a team member return empty optional.
   */
  public Optional<PwaTeamMember> getMembershipOfPersonInTeam(PwaTeam team, Person person) {
    return portalTeamAccessor.getPersonTeamMembership(person, team.getId())
        .map(ptm -> pwaTeamsDtoFactory.createPwaTeamMember(ptm, person, team));
  }

  /**
   * Wrap portalTeams API so calling code has easy way to determine person involvement in the regulator team.
   */
  public Optional<PwaRegulatorTeam> getRegulatorTeamIfPersonInRole(Person person, Collection<PwaRegulatorRole> roles) {

    if (roles.isEmpty()) {
      throw new IllegalArgumentException("Cannot check membership when no roles specified");
    }

    List<String> portalRoleNames = roles.stream()
        .map(PwaRegulatorRole::getPortalTeamRoleName)
        .collect(Collectors.toList());

    List<PortalTeamDto> regulatorTeamList = portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
        person,
        PwaTeamType.REGULATOR.getPortalTeamType(),
        portalRoleNames
    );

    if (regulatorTeamList.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(createRegulatorTeamOrError(regulatorTeamList));

  }

  /**
   * Wrap portalTeams API so calling code has easy way of determining person involvement across all Organisation teams.
   * Returns the Organisation Teams where the person is a member and has any of the provided roles
   */
  public List<PwaOrganisationTeam> getOrganisationTeamListIfPersonInRole(Person person, Collection<PwaOrganisationRole> roles) {
    if (roles.isEmpty()) {
      throw new IllegalArgumentException("Cannot check membership when no roles specified");
    }

    List<String> portalRoleNames = roles.stream()
        .map(PwaOrganisationRole::getPortalTeamRoleName)
        .collect(Collectors.toList());

    List<PortalTeamDto> orgTeamList = portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
        person,
        PwaTeamType.ORGANISATION.getPortalTeamType(),
        portalRoleNames
    );

    return pwaTeamsDtoFactory.createOrganisationTeamList(orgTeamList);
  }

  public boolean isUserInHolderTeam(Person person, Set<PortalOrganisationGroup> holderOrgGroups) {
    return getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .anyMatch(holderOrgGroups::contains);
  }

  private PwaRegulatorTeam createRegulatorTeamOrError(List<PortalTeamDto> teams) {
    if (teams.size() != 1) {
      throw new RuntimeException("Expected 1 REGULATOR type team but got " + teams.size());
    } else {
      return pwaTeamsDtoFactory.createRegulatorTeam(teams.get(0));
    }
  }

  /**
   * Remove a given person from a team.
   */
  public void removePersonFromTeam(PwaTeam team, Person personToRemove, WebUserAccount actionPerformedBy) {
    portalTeamAccessor.removePersonFromTeam(team.getId(), personToRemove, actionPerformedBy);
  }


  /**
   * Add (or update) the roles a given person in a team has.
   */
  public void addPersonToTeamInRoles(PwaTeam team, Person personToAdd, Collection<String> roleNames, WebUserAccount actionPerformedBy) {
    portalTeamAccessor.addPersonToTeamWithRoles(team.getId(), personToAdd, roleNames, actionPerformedBy);
  }

  /**
   * Check if a given Person has some role within a given PwaTeam.
   */
  public boolean isPersonMemberOfTeam(Person person, PwaTeam team) {
    return portalTeamAccessor.personIsAMemberOfTeam(team.getId(), person);
  }

  /**
   * Get a list of all possible roles members of a given PwaTeam can have.
   */
  public List<PwaRole> getAllRolesForTeam(PwaTeam team) {
    return portalTeamAccessor.getAllPortalRolesForTeam(team.getId()).stream()
        .map(pwaTeamsDtoFactory::createPwaRole)
        .collect(Collectors.toList());
  }

  /**
   * Get all organisation teams where user is a member.
   */
  public List<PwaOrganisationTeam> getOrganisationTeamsPersonIsMemberOf(Person person) {
    return getOrganisationTeamListIfPersonInRole(
        person,
        EnumSet.allOf(PwaOrganisationRole.class)
    );

  }

  public Set<PwaUserPrivilege> getAllUserPrivilegesForPerson(Person person) {

    // get privs available to the user through res type role membership
    var portalPrivSet = pwaTeamsDtoFactory.createPwaUserPrivilegeSet(
        portalTeamAccessor.getAllPortalSystemPrivilegesForPerson(person)
    );

    // get privs available to the user through PWA-only constructs
    var appPrivSet = pwaUserPrivilegeService.getPwaUserPrivilegesForPerson(person);

    var combinedPrivSet = SetUtils.union(portalPrivSet, appPrivSet);

    return Set.copyOf(combinedPrivSet);

  }

}
