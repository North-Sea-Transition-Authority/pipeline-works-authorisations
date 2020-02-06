package uk.co.ogauthority.pwa.service.teammanagement;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.teams.PortalTeamManagementController;
import uk.co.ogauthority.pwa.energyportal.model.WebUserAccountStatus;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.repository.PersonRepository;
import uk.co.ogauthority.pwa.energyportal.repository.WebUserAccountRepository;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;
import uk.co.ogauthority.pwa.mvc.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class TeamManagementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TeamManagementService.class);

  private final TeamService teamService;
  private final PersonRepository personRepository;
  private final WebUserAccountRepository webUserAccountRepository;

  public TeamManagementService(TeamService teamService,
                               PersonRepository personRepository,
                               WebUserAccountRepository webUserAccountRepository) {
    this.teamService = teamService;
    this.personRepository = personRepository;
    this.webUserAccountRepository = webUserAccountRepository;
  }

  public PwaTeam getTeamOrError(Integer resId) {
    return teamService.getTeamByResId(resId);
  }

  public Person getPerson(int personId) {
    return personRepository.findById(personId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Person with id " + personId + " not found"));
  }

  /**
   * Convert PwaTeamMember of a PwaTeam into TeamUserViews for team management screens.
   */
  public List<TeamMemberView> getTeamMemberViewsForTeam(PwaTeam team) {
    List<PwaTeamMember> pwaTeamMembers = teamService.getTeamMembers(team);

    List<TeamMemberView> users = new ArrayList<>();

    for (PwaTeamMember pwaTeamMember : pwaTeamMembers) {
      users.add(convertPwaTeamMemberToTeamUserView(pwaTeamMember));
    }

    return users;
  }

  /**
   * Return an optional wrapping a TeamMemberView of a person if they are a member of the provided team.
   */
  public Optional<TeamMemberView> getTeamMemberViewForTeamAndPerson(PwaTeam team, Person person) {
    return teamService.getMembershipOfPersonInTeam(team, person)
        .map(this::convertPwaTeamMemberToTeamUserView);

  }

  TeamMemberView convertPwaTeamMemberToTeamUserView(PwaTeamMember pwaTeamMember) {
    Person teamMemberPerson = pwaTeamMember.getPerson();
    PwaTeam team = pwaTeamMember.getTeam();

    Set<TeamRoleView> roleViews = pwaTeamMember.getRoleSet().stream()
        .map(TeamRoleView::createTeamRoleViewFrom)
        .collect(Collectors.toSet());

    String editRoute = ReverseRouter.route(on(PortalTeamManagementController.class).renderMemberRoles(
        team.getId(),
        teamMemberPerson.getId().asInt(),
        null,
        null
    ));

    String removeRoute = ReverseRouter.route(on(PortalTeamManagementController.class).renderRemoveTeamMember(
        team.getId(),
        teamMemberPerson.getId().asInt(),
        null
    ));

    return new TeamMemberView(
        teamMemberPerson,
        editRoute,
        removeRoute,
        roleViews
    );
  }

  public List<PwaTeam> getAllPwaTeamsUserCanManage(AuthenticatedUserAccount user) {
    List<PwaTeam> teamList = new ArrayList<>();

    List<PwaUserPrivilege> userPrivileges = teamService.getAllUserPrivilegesForPerson(user.getLinkedPerson());

    //If the logged in user is the team administrator for the regulator admin team then get the regulator team
    if (canManageRegulatorTeam(userPrivileges)) {
      PwaTeam regulatorTeam = teamService.getRegulatorTeam();
      teamList.add(regulatorTeam);
    }

    //If the logged in user is a regulator who can manage all organisation teams then get all organisations
    if (canManageAnyOrgTeam(userPrivileges)) {

      List<? extends PwaTeam> allOrgTeamList = teamService.getAllOrganisationTeams();
      allOrgTeamList.sort(Comparator.comparing(PwaTeam::getName));
      teamList.addAll(allOrgTeamList);

    } else {

      List<PwaOrganisationTeam> orgTeamsPersonAdminOf = teamService.getOrganisationTeamListIfPersonInRole(
          user.getLinkedPerson(),
          EnumSet.of(PwaOrganisationRole.TEAM_ADMINISTRATOR)
      );

      orgTeamsPersonAdminOf.sort(Comparator.comparing(PwaTeam::getName));

      teamList.addAll(orgTeamsPersonAdminOf);

    }
    return teamList;
  }


  /**
   * Populate the existing roles a person has for a given team.
   */
  public void populateExistingRoles(Person person, PwaTeam team, UserRolesForm form) {
    Optional<TeamMemberView> teamMember = getTeamMemberViewForTeamAndPerson(team, person);
    if (teamMember.isPresent()) {
      List<TeamRoleView> personRoles = new ArrayList<>(teamMember.get().getRoleViews());
      List<String> personRoleNames = personRoles.stream()
          .sorted(Comparator.comparing(TeamRoleView::getDisplaySequence))
          .map(TeamRoleView::getRoleName)
          .collect(Collectors.toList());
      form.setUserRoles(personRoleNames);

    } else {
      form.setUserRoles(new ArrayList<>());
    }

  }

  /**
   * Update the roles the given Person has in the given PwaTeam with those set in the UserRolesForm.
   * If the Person is a new member of the team, this also sends out an email to notify the person of their new roles.
   */
  @Transactional
  public void updateUserRoles(Person person, PwaTeam team, UserRolesForm form, WebUserAccount actionPerformedBy) {

    List<PwaRole> selectedRoles = getSelectedRolesForTeam(form, team);
    if (selectedRoles.isEmpty()) {
      throw new RuntimeException("Expected form with at least one selected role");
    }

    boolean isAlreadyTeamMember = teamService.isPersonMemberOfTeam(person, team);
    boolean settingAdminRole = selectedRoles.stream().anyMatch(PwaRole::isTeamAdministratorRole);

    if (isAlreadyTeamMember && isPersonLastTeamAdmin(team, person) && !settingAdminRole) {
      throw new LastAdministratorException("Operation would result in 0 team administrators");
    }

    if (isAlreadyTeamMember) {
      // Clear all roles so unselect roles are no longer applied
      teamService.removePersonFromTeam(team, person, actionPerformedBy);
    }

    List<String> roleNames = selectedRoles.stream()
        .map(PwaRole::getName)
        .collect(Collectors.toList());

    teamService.addPersonToTeamInRoles(team, person, roleNames, actionPerformedBy);

    if (!isAlreadyTeamMember) {
      // Only send a notification email if the user was not already in the team
      notifyNewTeamUser(team, person, selectedRoles);
    }
  }

  /**
   * Remove the given Person from the given PwaTeam, as long as they are not the last administrator for that team.
   */
  public void removeTeamMember(Person person,
                               PwaTeam team,
                               WebUserAccount actionPerformedBy) throws LastAdministratorException {

    if (isPersonMemberOfTeam(person, team)) {
      if (isPersonLastTeamAdmin(team, person)) {
        throw new LastAdministratorException(String.format(
            "PersonId %s cannot be removed from resId %s as this would result in 0 team admins", person.getId(), team.getId()
        ));
      } else {
        teamService.removePersonFromTeam(team, person, actionPerformedBy);
      }
    } else {
      throw new RuntimeException(String.format("PersonId %s is not a member of resId %s", person.getId(), team.getId()));
    }


  }

  private boolean isPersonLastTeamAdmin(PwaTeam team, Person person) {
    List<PwaTeamMember> teamAdministrators = getPwaTeamAdministrators(team);

    boolean personIsAdmin = teamAdministrators.stream()
        .anyMatch(tm -> tm.getPerson().equals(person));

    return personIsAdmin && teamAdministrators.size() == 1;
  }

  private List<PwaTeamMember> getPwaTeamAdministrators(PwaTeam team) {
    return teamService.getTeamMembers(team).stream()
        .filter(PwaTeamMember::isTeamAdministrator)
        .collect(Collectors.toList());
  }

  public List<PwaRole> getSelectedRolesForTeam(UserRolesForm form, PwaTeam team) {
    Map<String, PwaRole> selectableRolesForTeamMappedByName = teamService.getAllRolesForTeam(team).stream()
        .collect(Collectors.toMap((PwaRole::getName), (r -> r)));

    List<PwaRole> selectedRoles = new ArrayList<>();
    for (String roleName : form.getUserRoles()) {
      if (selectableRolesForTeamMappedByName.containsKey(roleName)) {
        selectedRoles.add(selectableRolesForTeamMappedByName.get(roleName));
      } else {
        LOGGER.error("Form contains roleNames not applicable for team resId: " + team.getId());
      }
    }

    return selectedRoles;
  }

  public List<TeamRoleView> getRolesForTeam(PwaTeam team) {
    return teamService.getAllRolesForTeam(team).stream()
        .map(TeamRoleView::createTeamRoleViewFrom)
        .sorted(Comparator.comparing(TeamRoleView::getDisplaySequence))
        .collect(Collectors.toList());
  }


  public void notifyNewTeamUser(PwaTeam team, Person person, List<PwaRole> selectedRoles) {
    // TODO PWA-149 - email notifications
    LOGGER.info("== TODO Email notification - PwaTeam member added ==");
  }

  /**
   * Checks if the given User has privileges to manage the given team.
   */
  public boolean canManageTeam(PwaTeam team, AuthenticatedUserAccount user) {
    // This does a full reload of privs which is slow.
    // Could use the ones cached against the AuthenticatedUserAccount if performance is an issue.
    List<PwaUserPrivilege> userPrivileges = teamService.getAllUserPrivilegesForPerson(user.getLinkedPerson());

    if (canManageAnyOrgTeam(userPrivileges) && team.getType().equals(PwaTeamType.ORGANISATION)) {
      // If the logged in user is a regulator with the organisation manage priv then they can manage any organisation team
      return true;
    } else {
      return teamService.getMembershipOfPersonInTeam(team, user.getLinkedPerson())
          .map(PwaTeamMember::isTeamAdministrator)
          .orElse(false);
    }
  }

  /**
   * Finds the Person linked to the WebUserAccount with the given email or loginId.
   */
  public Optional<Person> getPersonByEmailAddressOrLoginId(String emailOrLoginId) {

    List<WebUserAccount> webUserAccounts =
        webUserAccountRepository.findAllByEmailAddressAndAccountStatusNot(emailOrLoginId,
            WebUserAccountStatus.CANCELLED);

    if (webUserAccounts.size() == 1) {
      return Optional.of(webUserAccounts.get(0).getLinkedPerson());
    }

    webUserAccounts.addAll(
        webUserAccountRepository.findAllByLoginIdAndAccountStatusNot(emailOrLoginId, WebUserAccountStatus.CANCELLED));

    if (webUserAccounts.size() == 1) {
      return Optional.of(webUserAccounts.get(0).getLinkedPerson());
    } else {

      Set<Person> distinctPeople = webUserAccounts.stream()
          .map(WebUserAccount::getLinkedPerson)
          .collect(Collectors.toSet());

      if (distinctPeople.size() > 1) {
        throw new RuntimeException(
            String.format("getPersonByEmailAddressOrLoginId returned %d different people with email/loginId '%s'",
                distinctPeople.size(), emailOrLoginId)
        );
      } else if (distinctPeople.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(webUserAccounts.get(0).getLinkedPerson());
      }

    }
  }

  @VisibleForTesting
  boolean canManageRegulatorTeam(List<PwaUserPrivilege> userPrivileges) {
    return userPrivileges.stream()
        .anyMatch(p -> p.equals(PwaUserPrivilege.PWA_REGULATOR_ADMIN));
  }

  @VisibleForTesting
  boolean canManageAnyOrgTeam(List<PwaUserPrivilege> userPrivileges) {
    return userPrivileges.stream()
        .anyMatch(p -> p.equals(PwaUserPrivilege.PWA_REG_ORG_MANAGE));
  }

  public boolean isPersonMemberOfTeam(Person person, PwaTeam team) {
    return teamService.isPersonMemberOfTeam(person, team);
  }

}

