package uk.co.ogauthority.pwa.service.teammanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.teams.PortalTeamManagementController;
import uk.co.ogauthority.pwa.energyportal.model.WebUserAccountStatus;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccountTestUtil;
import uk.co.ogauthority.pwa.energyportal.repository.PersonRepository;
import uk.co.ogauthority.pwa.energyportal.repository.WebUserAccountRepository;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationUserRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorUserRole;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class TeamManagementServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private PersonRepository personRepository;

  @Mock
  private WebUserAccountRepository webUserAccountRepository;

  private PwaRegulatorTeam regulatorTeam;
  private PwaRole regTeamAdminRole;
  private PwaRole regTeamSomeOtherRole;
  private PwaTeamMember regulatorPersonRegulatorTeamMember;
  private PwaOrganisationTeam organisationTeam1;
  private PwaOrganisationTeam organisationTeam2;
  private Person regulatorTeamAdminPerson;
  private Person otherRegulatorPerson;
  private Person organisationPerson;
  private AuthenticatedUserAccount organisationUser;
  private AuthenticatedUserAccount manageAnyOrgRegulatorUser;
  private AuthenticatedUserAccount manageRegTeamRegulatorUser;
  private AuthenticatedUserAccount workareaOnlyUser;
  private AuthenticatedUserAccount manageAllTeamsUser;
  private WebUserAccount someWebUserAccount = new WebUserAccount(99);
  private UserRolesForm userRolesForm;
  private TeamManagementService teamManagementService;

  /**
   * This creates:
   * - A regulator team with 1 member (regulatorTeamAdminPerson) who has admin privs only. This team has 2 roles, regTeamAdminRole
   * and regTeamSomeOtherRole.
   * - 2 organisation teams in separate org groupsL organisationTeam1 and organisationTeam2
   * - Several AuthenticatedUserAccount with various privileges
   */
  @Before
  public void setUp() {

    teamManagementService = new TeamManagementService(teamService, personRepository, webUserAccountRepository);

    regulatorTeam = TeamTestingUtils.getRegulatorTeam();

    regTeamAdminRole = TeamTestingUtils.getTeamAdminRole();
    regTeamSomeOtherRole = TeamTestingUtils.generatePwaRole("SOME_ROLE", 999);

    regulatorTeamAdminPerson = new Person(1, "reg", "person", "reg@person.com", "0");
    regulatorPersonRegulatorTeamMember = new PwaTeamMember(regulatorTeam, regulatorTeamAdminPerson, Set.of(regTeamAdminRole));

    otherRegulatorPerson = new Person(2, "other reg", "person", "otherreg@person.com", "0");

    organisationPerson = new Person(3, "org", "person", "org@person.com", "0");
    organisationUser = new AuthenticatedUserAccount(new WebUserAccount(3, organisationPerson), List.of());

    var manageAnyOrgRegulatorPerson = new Person(4, "regOrg", "manage", "regOrg@manage.com", "0");
    manageAnyOrgRegulatorUser = new AuthenticatedUserAccount(new WebUserAccount(4, manageAnyOrgRegulatorPerson), List.of());
    when(teamService.getAllUserPrivilegesForPerson(manageAnyOrgRegulatorPerson))
        .thenReturn(Set.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE));

    var manageRegTeamRegulatorPerson = new Person(5, "regTeam", "manage", "regTeam@manage.com", "0");
    manageRegTeamRegulatorUser = new AuthenticatedUserAccount(new WebUserAccount(5, manageRegTeamRegulatorPerson), List.of());
    when(teamService.getAllUserPrivilegesForPerson(manageRegTeamRegulatorPerson))
        .thenReturn(Set.of(PwaUserPrivilege.PWA_REGULATOR_ADMIN));

    var manageAllTeamsPerson = new Person(6, "all", "manage", "all@manage.com", "0");
    manageAllTeamsUser = new AuthenticatedUserAccount(new WebUserAccount(6, manageAllTeamsPerson), List.of());
    when(teamService.getAllUserPrivilegesForPerson(manageAllTeamsPerson))
        .thenReturn(Set.of(PwaUserPrivilege.PWA_REGULATOR_ADMIN, PwaUserPrivilege.PWA_REG_ORG_MANAGE));

    var workareaOnlyPerson = new Person(7, "workarea", "only", "workarea@only.com", "0");
    workareaOnlyUser = new AuthenticatedUserAccount(new WebUserAccount(7, workareaOnlyPerson), List.of());
    when(teamService.getAllUserPrivilegesForPerson(workareaOnlyPerson))
        .thenReturn(Set.of(PwaUserPrivilege.PWA_WORKAREA));

    var portalOrganisationGroup1 = TeamTestingUtils.generateOrganisationGroup(111, "GROUP1", "GRP1");
    organisationTeam1 = new PwaOrganisationTeam(222, "team1", "team1", portalOrganisationGroup1);

    var portalOrganisationGroup2 = TeamTestingUtils.generateOrganisationGroup(333, "GROUP2", "GRP2");
    organisationTeam2 = new PwaOrganisationTeam(444, "team2", "team2", portalOrganisationGroup2);


    when(teamService.getRegulatorTeam()).thenReturn(regulatorTeam);

    when(teamService.getTeamByResId(regulatorTeam.getId())).thenReturn(regulatorTeam);

    when(teamService.getMembershipOfPersonInTeam(eq(regulatorTeam), eq(regulatorTeamAdminPerson)))
        .thenReturn(Optional.of(regulatorPersonRegulatorTeamMember));

    when(teamService.getTeamMembers(regulatorTeam))
        .thenReturn(List.of(regulatorPersonRegulatorTeamMember));

    when(teamService.isPersonMemberOfTeam(regulatorTeamAdminPerson, regulatorTeam)).thenReturn(true);

    when(teamService.getAllRolesForTeam(regulatorTeam))
        .thenReturn(List.of(regTeamAdminRole, regTeamSomeOtherRole));

    when(teamService.getAllOrganisationTeams())
        // Returned list must be mutable
        .thenReturn(new ArrayList<>(List.of(organisationTeam1, organisationTeam2)));

    userRolesForm = new UserRolesForm();
  }

  @Test
  public void getTeamOrError_verifyServiceInteraction() {
    PwaTeam regTeam = teamManagementService.getTeamOrError(regulatorTeam.getId());
    assertThat(regTeam).isEqualTo(regulatorTeam);
    verify(teamService, times(1)).getTeamByResId(eq(regulatorTeam.getId()));
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getTeamOrError_throwErrorWhenTeamNotFound() {
    when(teamService.getTeamByResId(anyInt())).thenThrow(new PwaEntityNotFoundException(""));
    teamManagementService.getTeamOrError(999);
  }

  @Test
  public void getTeamMemberViewForTeamAndPerson_whenNotATeamMember() {
    assertThat(teamManagementService.getTeamMemberViewForTeamAndPerson(regulatorTeam, organisationPerson)).isEmpty();
  }

  @Test
  public void getTeamMemberViewForTeamAndPerson_whenATeamMember_basicPersonPropertiesMappedAsExpected() {
    var teamMemberView = teamManagementService.getTeamMemberViewForTeamAndPerson(regulatorTeam,
        regulatorTeamAdminPerson).get();
    assertTeamUserViewHasExpectedSimpleProperties(regulatorTeam, regulatorTeamAdminPerson, teamMemberView);

  }

  @Test
  public void getTeamMemberViewForTeamAndPerson_whenATeamMember_memberRolesMappedAsExpected() {

    var teamMemberView = teamManagementService.getTeamMemberViewForTeamAndPerson(regulatorTeam,
        regulatorTeamAdminPerson).get();

    PwaRole expectedRole = TeamTestingUtils.getTeamAdminRole();
    assertTeamUserViewHasSingleRoleMappedAsExpected(expectedRole, teamMemberView);
  }

  @Test
  public void getTeamMemberViewsForTeam_whenSingleTeamMemberIsAdmin() {
    PwaRole expectedRole = TeamTestingUtils.getTeamAdminRole();
    List<TeamMemberView> teamMemberViewsForTeam = teamManagementService.getTeamMemberViewsForTeam(regulatorTeam);

    assertThat(teamMemberViewsForTeam).hasSize(1);
    assertTeamUserViewHasSingleRoleMappedAsExpected(expectedRole, teamMemberViewsForTeam.get(0));
    assertTeamUserViewHasExpectedSimpleProperties(regulatorTeam, regulatorTeamAdminPerson, teamMemberViewsForTeam.get(0));
  }

  @Test
  public void getRolesForTeam_orderedByDisplaySequenceValue() {
    var firstRole = TeamTestingUtils.generatePwaRole("FIRST_ROLE", 10);
    var secondRole = TeamTestingUtils.generatePwaRole("SECOND_ROLE", 20);
    var thirdRole = TeamTestingUtils.generatePwaRole("THIRD_ROLE", 30);
    List<PwaRole> unorderedRoleList = List.of(secondRole, thirdRole, firstRole);

    when(teamService.getAllRolesForTeam(regulatorTeam)).thenReturn(unorderedRoleList);

    List<TeamRoleView> selectableViews = teamManagementService.getRolesForTeam(regulatorTeam);
    assertThat(selectableViews.get(0).getRoleName()).isEqualTo(firstRole.getName());
    assertThat(selectableViews.get(0).getDisplaySequence()).isEqualTo(firstRole.getDisplaySequence());

    assertThat(selectableViews.get(1).getRoleName()).isEqualTo(secondRole.getName());
    assertThat(selectableViews.get(1).getDisplaySequence()).isEqualTo(secondRole.getDisplaySequence());

    assertThat(selectableViews.get(2).getRoleName()).isEqualTo(thirdRole.getName());
    assertThat(selectableViews.get(2).getDisplaySequence()).isEqualTo(thirdRole.getDisplaySequence());
  }

  @Test
  public void canManageTeam_whenUserIsMemberOfTeam_andIsTeamAdministrator() {
    var teamMember = new PwaTeamMember(organisationTeam1, organisationPerson, Set.of(TeamTestingUtils.getTeamAdminRole()));
    when(teamService.getMembershipOfPersonInTeam(organisationTeam1, organisationPerson))
        .thenReturn(Optional.of(teamMember));

    assertThat(teamManagementService.canManageTeam(organisationTeam1, organisationUser)).isTrue();
  }

  @Test
  public void canManageTeam_whenUserIsMemberOfTeam_andNotTeamAdministrator() {
    var teamMember = new PwaTeamMember(
        organisationTeam1,
        organisationPerson,
        Set.of(TeamTestingUtils.generatePwaRole("Some non admin role", 999))
    );
    when(teamService.getMembershipOfPersonInTeam(organisationTeam1, organisationPerson)).thenReturn(
        Optional.of(teamMember));

    assertThat(teamManagementService.canManageTeam(organisationTeam1, organisationUser)).isFalse();
  }


  @Test
  public void canManageTeam_whenUserCanManageAnyOrgTeamOnly_AndTeamIsRegulator() {
    assertThat(teamManagementService.canManageTeam(regulatorTeam, manageAnyOrgRegulatorUser)).isFalse();
  }

  @Test
  public void canManageTeam_whenUserCanManageAnyOrgTeamOnly_AndTeamIsOrganisation() {
    assertThat(teamManagementService.canManageTeam(organisationTeam1, manageAnyOrgRegulatorUser)).isTrue();
  }

  @Test
  public void getAllPwaTeamsUserCanManage_userCannotManageAnyTeams() {
    assertThat(teamManagementService.getAllPwaTeamsUserCanManage(workareaOnlyUser)).isEmpty();
  }

  @Test
  public void getAllPwaTeamsUserCanManage_userCanManageRegulatorTeamOnly() {
    List<PwaTeam> manageableTeams = teamManagementService.getAllPwaTeamsUserCanManage(manageRegTeamRegulatorUser);
    assertThat(manageableTeams).containsExactly(regulatorTeam);
  }

  @Test
  public void getAllPwaTeamsUserCanManage_userCanManageAllOrgTeamsOnly() {
    List<PwaTeam> manageableTeams = teamManagementService.getAllPwaTeamsUserCanManage(manageAnyOrgRegulatorUser);
    assertThat(manageableTeams).containsExactly(organisationTeam1, organisationTeam2);
  }

  @Test
  public void getAllIrsTeamsUserCanManage_userCanManageRegulatorTeamAndAllOrgs() {
    List<PwaTeam> manageableTeams = teamManagementService.getAllPwaTeamsUserCanManage(manageAllTeamsUser);
    assertThat(manageableTeams).containsExactly(regulatorTeam, organisationTeam1, organisationTeam2);
  }

  @Test
  public void populateExistingRoles_personHasNoRolesInTeam() {
    when(teamService.getMembershipOfPersonInTeam(organisationTeam1, regulatorTeamAdminPerson))
        .thenReturn(Optional.empty());

    teamManagementService.populateExistingRoles(regulatorTeamAdminPerson, organisationTeam1, userRolesForm);
    assertThat(userRolesForm.getUserRoles()).isEmpty();
  }

  @Test
  public void populateExistingRoles_personHasRolesInTeam() {
    when(teamService.getMembershipOfPersonInTeam(regulatorTeam, regulatorTeamAdminPerson))
        .thenReturn(Optional.of(new PwaTeamMember(regulatorTeam,
            regulatorTeamAdminPerson, Set.of(regTeamAdminRole, regTeamSomeOtherRole))));

    teamManagementService.populateExistingRoles(regulatorTeamAdminPerson, regulatorTeam, userRolesForm);
    // Expect order to match role display sequence
    assertThat(userRolesForm.getUserRoles()).containsExactly(regTeamAdminRole.getName(),
        regTeamSomeOtherRole.getName());

  }

  @Test
  public void removePersonFromTeam_personBeingRemovedIsNotLastAdministrator() {
    // Setup a second team member who is also an admin
    var secondTeamMember = new PwaTeamMember(regulatorTeam, otherRegulatorPerson, Set.of(regTeamAdminRole));

    when(teamService.getTeamMembers(regulatorTeam))
        .thenReturn(List.of(regulatorPersonRegulatorTeamMember, secondTeamMember));

    when(teamService.isPersonMemberOfTeam(or(eq(regulatorPersonRegulatorTeamMember.getPerson()), eq(secondTeamMember.getPerson())), eq(regulatorTeam)))
        .thenReturn(true);

    teamManagementService.removeTeamMember(regulatorTeamAdminPerson, regulatorTeam, someWebUserAccount);
    verify(teamService, times(1)).removePersonFromTeam(regulatorTeam, regulatorTeamAdminPerson, someWebUserAccount);
  }

  @Test(expected = LastAdministratorException.class)
  public void removePersonFromTeam_personBeingRemovedIsLastAdministrator() {
    // Setup a second team member who is not an admin
    var secondTeamMember = new PwaTeamMember(regulatorTeam, otherRegulatorPerson, Set.of(regTeamSomeOtherRole));

    when(teamService.getTeamMembers(regulatorTeam))
        .thenReturn(List.of(regulatorPersonRegulatorTeamMember, secondTeamMember));

    when(teamService.isPersonMemberOfTeam(or(eq(regulatorPersonRegulatorTeamMember.getPerson()), eq(secondTeamMember.getPerson())), eq(regulatorTeam)))
        .thenReturn(true);

    teamManagementService.removeTeamMember(regulatorTeamAdminPerson, regulatorTeam, someWebUserAccount);
  }

  @Test(expected = RuntimeException.class)
  public void updateUserRoles_errorsWhenFormHasNoRoles() {
    userRolesForm.setUserRoles(List.of());
    teamManagementService.updateUserRoles(regulatorTeamAdminPerson, regulatorTeam, userRolesForm, someWebUserAccount);
  }

  @Test(expected = LastAdministratorException.class)
  public void updateUserRoles_errorsWhenLastAdminIsUpdatedAsNoLongerAnAdmin() {
    userRolesForm.setUserRoles(List.of(regTeamSomeOtherRole.getName()));
    teamManagementService.updateUserRoles(regulatorTeamAdminPerson, regulatorTeam, userRolesForm, someWebUserAccount);
  }

  @Test
  public void updateUserRoles_whenPersonWhoIsLastAdminHasRoleAdded() {
    userRolesForm.setUserRoles(List.of(regTeamAdminRole.getName(), regTeamSomeOtherRole.getName()));

    teamManagementService.updateUserRoles(regulatorTeamAdminPerson, regulatorTeam, userRolesForm, someWebUserAccount);

    var orderVerifier = Mockito.inOrder(teamService);
    orderVerifier.verify(teamService).removePersonFromTeam(regulatorTeam, regulatorTeamAdminPerson, someWebUserAccount);
    orderVerifier.verify(teamService).addPersonToTeamInRoles(regulatorTeam, regulatorTeamAdminPerson, userRolesForm.getUserRoles(), someWebUserAccount);
    orderVerifier.verifyNoMoreInteractions();
  }

  @Test
  public void updateUserRoles_whenNewPersonAddedToTeam() {
    when(teamService.isPersonMemberOfTeam(regulatorTeamAdminPerson, regulatorTeam)).thenReturn(false);
    userRolesForm.setUserRoles(List.of(regTeamAdminRole.getName(), regTeamSomeOtherRole.getName()));

    teamManagementService.updateUserRoles(regulatorTeamAdminPerson, regulatorTeam, userRolesForm, someWebUserAccount);

    var orderVerifier = Mockito.inOrder(teamService);
    orderVerifier.verify(teamService).addPersonToTeamInRoles(regulatorTeam, regulatorTeamAdminPerson, userRolesForm.getUserRoles(), someWebUserAccount);
    orderVerifier.verifyNoMoreInteractions();
  }

  @Test
  public void getSelectedRolesForTeam_formContainsOnlyUnsupportedRolesForTeam() {
    userRolesForm.setUserRoles(List.of("NOT_SUPPORTED1", "NOT_SUPPORTED2"));

    List<PwaRole> validSelectedRoles = teamManagementService.getSelectedRolesForTeam(userRolesForm, regulatorTeam);
    assertThat(validSelectedRoles).isEmpty();
  }

  @Test
  public void getSelectedRolesForTeam_formContainsSomeSupportedRolesForTeam() {
    userRolesForm.setUserRoles(List.of("NOT_SUPPORTED1", "NOT_SUPPORTED2", regTeamAdminRole.getName(), regTeamSomeOtherRole.getName()));

    List<PwaRole> validSelectedRoles = teamManagementService.getSelectedRolesForTeam(userRolesForm, regulatorTeam);
    assertThat(validSelectedRoles).containsExactlyInAnyOrder(regTeamAdminRole, regTeamSomeOtherRole);
  }

  @Test
  public void canManageAnyOrgTeam() {
    assertThat(teamManagementService.canManageAnyOrgTeam(Set.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE))).isTrue();
    assertThat(teamManagementService.canManageAnyOrgTeam(Set.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_REG_ORG_MANAGE))).isTrue();

    assertThat(teamManagementService.canManageAnyOrgTeam(Set.of(PwaUserPrivilege.PWA_WORKAREA))).isFalse();
    assertThat(teamManagementService.canManageAnyOrgTeam(Set.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_REGULATOR_ADMIN))).isFalse();
  }

  @Test
  public void canManageRegulatorTeam() {
    assertThat(teamManagementService.canManageRegulatorTeam(Set.of(PwaUserPrivilege.PWA_REGULATOR_ADMIN))).isTrue();
    assertThat(teamManagementService.canManageRegulatorTeam(Set.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_REGULATOR_ADMIN))).isTrue();

    assertThat(teamManagementService.canManageRegulatorTeam(Set.of(PwaUserPrivilege.PWA_WORKAREA))).isFalse();
    assertThat(teamManagementService.canManageRegulatorTeam(Set.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_REG_ORG_MANAGE))).isFalse();
  }



  private void assertTeamUserViewHasSingleRoleMappedAsExpected(PwaRole expectedRole,
                                                               TeamMemberView teamUserView) {
    Set<TeamRoleView> roleViews = teamUserView.getRoleViews();

    assertThat(roleViews).hasSize(1);
    for (TeamRoleView roleView : roleViews) {
      assertThat(roleView.getRoleName()).isEqualTo(expectedRole.getName());
      assertThat(roleView.getTitle()).isEqualTo(expectedRole.getTitle());
      assertThat(roleView.getDescription()).isEqualTo(expectedRole.getDescription());
      assertThat(roleView.getDisplayName()).isEqualTo(expectedRole.getDescription());
      assertThat(roleView.getIdentifier()).isEqualTo(expectedRole.getName());
    }
  }

  private void assertTeamUserViewHasExpectedSimpleProperties(PwaTeam team, Person person, TeamMemberView teamUserView) {
    String expectedEditRoute = ReverseRouter.route(on(PortalTeamManagementController.class).renderMemberRoles(
        team.getId(),
        person.getId().asInt(),
        null,
        null
    ));

    String expectedRemoveRoute = ReverseRouter.route(on(PortalTeamManagementController.class).renderRemoveTeamMember(
        team.getId(),
        person.getId().asInt(),
        null
    ));

    assertThat(teamUserView.getForename()).isEqualTo(person.getForename());
    assertThat(teamUserView.getSurname()).isEqualTo(person.getSurname());
    assertThat(teamUserView.getFullName()).isEqualTo(person.getForename() + " " + person.getSurname());
    assertThat(teamUserView.getEditRoute()).isEqualTo(expectedEditRoute);
    assertThat(teamUserView.getRemoveRoute()).isEqualTo(expectedRemoveRoute);
  }

  @Test
    public void getUserRolesForPwaTeam_regulatorTeamType() {
      var pwaTeam = new PwaRegulatorTeam(1, null, null);
      var userRoles = teamManagementService.getUserRolesForPwaTeam(pwaTeam);
      assertThat(userRoles).containsAll(PwaRegulatorUserRole.stream().collect(Collectors.toList()));
  }

  @Test
    public void getUserRolesForPwaTeam_organisationTeamType() {
      var pwaTeam = new PwaOrganisationTeam(1, null, null, null);
      var userRoles = teamManagementService.getUserRolesForPwaTeam(pwaTeam);
      assertThat(userRoles).containsAll(PwaOrganisationUserRole.stream().collect(Collectors.toList()));
  }


  @Test
  public void getPersonByEmailAddressOrLoginId_exactly1WuaFoundByEmailSearch_personReturned() {

    var person = PersonTestUtil.createDefaultPerson();
    var user = WebUserAccountTestUtil.createWebUserAccountMatchingPerson(1, person, WebUserAccountStatus.ACTIVE);

    when(webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(
        person.getEmailAddress(), List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(List.of(user));

    var personOptional = teamManagementService.getPersonByEmailAddressOrLoginId(person.getEmailAddress());

    assertThat(personOptional).isPresent();
    assertThat(personOptional.get().getEmailAddress()).isEqualTo(person.getEmailAddress());
  }

  @Test
  public void getPersonByEmailAddressOrLoginId_exactly1WuaFoundByLoginIdSearch_personReturned() {

    var person = PersonTestUtil.createDefaultPerson();
    var user = WebUserAccountTestUtil.createWebUserAccount(1, person, "myLoginId", WebUserAccountStatus.ACTIVE);

    when(webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(
        user.getLoginId(), List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(List.of(user));


    var personOptional = teamManagementService.getPersonByEmailAddressOrLoginId(user.getLoginId());

    assertThat(personOptional).isPresent();
    assertThat(personOptional.get().getEmailAddress()).isEqualTo(person.getEmailAddress());
  }

  //An unlikely potential situation where two people have the same email address but with different user accounts that are active causing an error
  @Test(expected = RuntimeException.class)
  public void getPersonByEmailAddressOrLoginId_multiplePeopleFoundForSameEmail_exceptionThrown() {

    var emailAddress = "me@email.com";
    var person1 = PersonTestUtil.createPersonFrom(new PersonId(1), emailAddress);
    var user1 = WebUserAccountTestUtil.createWebUserAccount(1, person1, "myLoginId1", WebUserAccountStatus.ACTIVE);

    var person2 = PersonTestUtil.createPersonFrom(new PersonId(2), emailAddress);
    var user2 = WebUserAccountTestUtil.createWebUserAccount(1, person2, "myLoginId2", WebUserAccountStatus.ACTIVE);

    var user3 = WebUserAccountTestUtil.createWebUserAccount(2, person2, emailAddress, WebUserAccountStatus.ACTIVE);

    when(webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(
        emailAddress, List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(new ArrayList<>(List.of(user1, user2)));

    when(webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(
        user3.getLoginId(), List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(List.of(user3));

    teamManagementService.getPersonByEmailAddressOrLoginId(person1.getEmailAddress());
  }

  @Test
  public void getPersonByEmailAddressOrLoginId_noPersonFoundForEmail_noPersonReturned() {

    when(webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(any(), any())).thenReturn(new ArrayList<>());
    when(webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(any(), any())).thenReturn(List.of());

    var personOptional = teamManagementService.getPersonByEmailAddressOrLoginId("me@email.com");
    assertThat(personOptional).isEmpty();
  }

  @Test
  public void getPersonByEmailAddressOrLoginId_multipleWuaForEmail_exactly1PersonForAccounts_personReturned() {

    var emailAddress = "me@email.com";
    var person1 = PersonTestUtil.createPersonFrom(new PersonId(1), emailAddress);
    var user1 = WebUserAccountTestUtil.createWebUserAccount(1, person1, "myLoginId1", WebUserAccountStatus.ACTIVE);
    var user2 = WebUserAccountTestUtil.createWebUserAccount(1, person1, "myLoginId2", WebUserAccountStatus.ACTIVE);


    when(webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(
        emailAddress, List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(new ArrayList<>(List.of(user1, user2)));

    when(webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(any(), any())).thenReturn(List.of());

    var personOptional = teamManagementService.getPersonByEmailAddressOrLoginId(emailAddress);
    assertThat(personOptional).isPresent();
    assertThat(personOptional.get().getEmailAddress()).isEqualTo(emailAddress);
  }





}