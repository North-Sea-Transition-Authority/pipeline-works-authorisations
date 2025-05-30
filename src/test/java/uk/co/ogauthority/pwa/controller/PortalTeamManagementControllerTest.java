package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlTemplate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.teams.PortalTeamManagementController;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.model.teams.PwaTeam;
import uk.co.ogauthority.pwa.service.teammanagement.AddUserToTeamFormValidator;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.service.teammanagement.OldTeamManagementService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@WebMvcTest(PortalTeamManagementController.class)
@Import(PwaMvcTestConfiguration.class)
class PortalTeamManagementControllerTest extends AbstractControllerTest {

  private static final int UNKNOWN_PERSON_ID = 123456789;
  private static final int UNKNOWN_RES_ID = 99999;

  @MockBean
  private OldTeamManagementService teamManagementService;

  @MockBean
  protected AddUserToTeamFormValidator addUserToTeamFormValidator;

  @Value("${oga.registration.link}")
  private String ogaRegistrationLink;

  private AuthenticatedUserAccount regulatorTeamAdmin;
  private Person regulatorTeamAdminPerson;
  private AuthenticatedUserAccount organisationTeamAdmin;
  private Person organisationTeamAdminPerson;
  private PwaTeam regulatorTeam;
  private PwaTeam organisationTeam;
  private TeamRoleView teamAdminRole;
  private TeamMemberView regTeamAdminTeamUserView;


  @BeforeEach
  void teamManagementTestSetup() {

    regulatorTeamAdmin = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_REGULATOR_ADMIN));
    organisationTeamAdmin = new AuthenticatedUserAccount(new WebUserAccount(2), List.of(PwaUserPrivilege.PWA_ORG_ADMIN));

    regulatorTeamAdminPerson = new Person(1, "Regulator", "Admin", "reg@admin.org", "0");
    organisationTeamAdminPerson = new Person(2, "Organisation", "Admin", "org@admin.org", "0");

    regulatorTeam = TeamTestingUtils.getRegulatorTeam();
    organisationTeam = TeamTestingUtils.getOrganisationTeam(
        PortalOrganisationTestUtils.generateOrganisationGroup(100, "ORGANISATION_GROUP", "ORG_GRP")
    );

    when(teamManagementService.getTeamOrError(regulatorTeam.getId())).thenReturn(regulatorTeam);
    when(teamManagementService.getTeamOrError(UNKNOWN_RES_ID)).thenThrow(new PwaEntityNotFoundException(""));

    when(teamManagementService.canManageTeam(regulatorTeam, regulatorTeamAdmin)).thenReturn(true);
    when(teamManagementService.canManageTeam(regulatorTeam, organisationTeamAdmin)).thenReturn(false);

    when(teamManagementService.getPerson(regulatorTeamAdminPerson.getId().asInt())).thenReturn(regulatorTeamAdminPerson);
    when(teamManagementService.getPerson(UNKNOWN_PERSON_ID)).thenThrow(new PwaEntityNotFoundException(""));

    teamAdminRole = TeamRoleView.createTeamRoleViewFrom(TeamTestingUtils.getTeamAdminRole());
    regTeamAdminTeamUserView = new TeamMemberView(
        regulatorTeamAdminPerson,
        "some/edit/route",
        "some/remove/route",
        Set.of(teamAdminRole)
    );

    when(teamManagementService.getTeamMemberViewForTeamAndPerson(regulatorTeam, regulatorTeamAdminPerson))
        .thenReturn(Optional.of(regTeamAdminTeamUserView));

  }

  @Test
  void renderManageableTeams_whenMultipleTeamsCanBeManaged() throws Exception {
    when(teamManagementService.getAllPwaTeamsUserCanManage(regulatorTeamAdmin))
        .thenReturn(List.of(regulatorTeam, organisationTeam));

    mockMvc.perform(get("/portal-team-management")
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isOk());
  }

  @Test
  void renderManageableTeams_whenSingleTeamCanBeManaged() throws Exception {
    when(teamManagementService.getAllPwaTeamsUserCanManage(regulatorTeamAdmin))
        .thenReturn(List.of(regulatorTeam));

    mockMvc.perform(get("/portal-team-management")
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void renderManageableTeams_whenZeroTeamsCanBeManaged() throws Exception {
    when(teamManagementService.getAllPwaTeamsUserCanManage(regulatorTeamAdmin))
        .thenReturn(List.of());

    mockMvc.perform(get("/portal-team-management")
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamMembers_whenTeamFound_andUserCanManageTeam() throws Exception {
    when(teamManagementService.getTeamMemberViewsForTeam(regulatorTeam))
        .thenReturn(List.of(regTeamAdminTeamUserView));

    mockMvc.perform(get("/portal-team-management/teams/{resId}/member", regulatorTeam.getId())
        .with(user(regulatorTeamAdmin)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(view().name("teamManagementOld/teamMembers"));
  }

  @Test
  void renderTeamMembers_whenTeamFound_andUserCannotManageTeam() throws Exception {
    mockMvc.perform(get("/portal-team-management/teams/{resId}/member", regulatorTeam.getId())
        .with(user(organisationTeamAdmin)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamMembers_whenTeamNotFound() throws Exception {
    mockMvc.perform(get("/portal-team-management/teams/{resId}/member", UNKNOWN_RES_ID)
        .with(user(regulatorTeamAdmin)))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void renderMemberRoles_whenTeamFound_andUserCanManage_andFormNotFilled() throws Exception {
    mockMvc.perform(
        get("/portal-team-management/teams/{resId}/member/{personId}/roles",
            regulatorTeam.getId(),
            regulatorTeamAdminPerson.getId().asInt())
            .requestAttr("form", new UserRolesForm())
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isOk())
        .andExpect(view().name("teamManagementOld/memberRoles"));

    verify(teamManagementService, times(1)).populateExistingRoles(
        eq(regulatorTeamAdminPerson),
        eq(regulatorTeam),
        any()
    );
  }

  @Test
  void renderMemberRoles_whenTeamFound_andUserCanManage_andFormFilledWithRoles() throws Exception {
    mockMvc.perform(
        get("/portal-team-management/teams/{resId}/member/{personId}/roles",
            regulatorTeam.getId(),
            regulatorTeamAdminPerson.getId().asInt())
        .param("userRoles", teamAdminRole.getRoleName())
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isOk())
        .andExpect(view().name("teamManagementOld/memberRoles"));

    verify(teamManagementService, times(0)).populateExistingRoles(
        eq(regulatorTeamAdminPerson),
        eq(regulatorTeam),
        any()
    );
  }

  @Test
  void renderMemberRoles_whenPersonNotFoundTest() throws Exception {
    mockMvc.perform(
        get("/portal-team-management/teams/{resId}/member/{personId}/roles",
            regulatorTeam.getId(),
            UNKNOWN_PERSON_ID)
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderMemberRoles_whenTeamNotFoundTest() throws Exception {
    mockMvc.perform(
        get("/portal-team-management/teams/{resId}/member/{personId}/roles",
            UNKNOWN_RES_ID,
            regulatorTeamAdminPerson.getId().asInt())
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isNotFound());
  }

  @Test
  void handleMemberRolesUpdate_whenUserCanManageTeam_andNoBindingErrors_andNewMemberAddedToTeam() throws Exception {
    mockMvc.perform(
        post("/portal-team-management/teams/{resId}/member/{personId}/roles"
            , regulatorTeam.getId()
            , regulatorTeamAdminPerson.getId().asInt())
        .with(user(regulatorTeamAdmin))
        .param("userRoles", teamAdminRole.getRoleName())
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlTemplate("/portal-team-management/teams/{resId}/member", regulatorTeam.getId()));

    verify(teamManagementService, times(1)).updateUserRoles(
        eq(regulatorTeamAdminPerson),
        eq(regulatorTeam),
        any(UserRolesForm.class),
        eq(regulatorTeamAdmin)
    );
  }

  @Test
  void handleMemberRolesUpdate_whenUserCanManageTeam_andNoBindingErrors_andRemovingLastTeamAdmin() throws Exception {
    doThrow(new LastAdministratorException(""))
      .when(teamManagementService).updateUserRoles(
        eq(regulatorTeamAdminPerson),
        eq(regulatorTeam),
        any(UserRolesForm.class),
        eq(regulatorTeamAdmin)
      );

    mockMvc.perform(
        post("/portal-team-management/teams/{resId}/member/{personId}/roles"
            , regulatorTeam.getId()
            , regulatorTeamAdminPerson.getId().asInt())
        .with(user(regulatorTeamAdmin))
        .with(csrf())
        .param("userRoles", teamAdminRole.getRoleName()))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("form"));

    verify(teamManagementService, times(1)).updateUserRoles(
        eq(regulatorTeamAdminPerson),
        eq(regulatorTeam),
        any(UserRolesForm.class),
        eq(regulatorTeamAdmin)
    );
  }

  @Test
  void handleMemberRolesUpdate_whenUserCanManageTeam_andFromValidationProducesErrors() throws Exception {
    mockMvc.perform(
        post("/portal-team-management/teams/{resId}/member/{personId}/roles"
            , regulatorTeam.getId()
            , regulatorTeamAdminPerson.getId().asInt())
        .with(user(regulatorTeamAdmin))
        .with(csrf())
        .param("userRoles", ""))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("form"));

    verify(teamManagementService, times(0)).updateUserRoles(any(), any(), any(), any());
    verify(teamManagementService, times(0)).notifyNewTeamUser(any(), any(), any());
  }

  @Test
  void handleMemberRolesUpdate_whenUserCannotManageTeam() throws Exception {
    mockMvc.perform(
        post("/portal-team-management/teams/{resId}/member/{personId}/roles"
            , regulatorTeam.getId()
            , regulatorTeamAdminPerson.getId().asInt())
        .with(user(organisationTeamAdmin))
        .with(csrf())
        .param("userRoles", ""))
        .andExpect(status().isForbidden());

    verify(teamManagementService, times(0)).updateUserRoles(any(), any(), any(), any());
    verify(teamManagementService, times(0)).notifyNewTeamUser(any(), any(), any());
  }


  @Test
  void renderRemoveTeamMember_whenNotATeamMember() throws Exception {
    when(teamManagementService.getTeamMemberViewForTeamAndPerson(regulatorTeam, regulatorTeamAdminPerson))
        .thenReturn(Optional.empty());

    mockMvc.perform(
        get("/portal-team-management/teams/{resId}/member/{personId}/remove",
            regulatorTeam.getId(),
            regulatorTeamAdminPerson.getId().asInt())
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderRemoveTeamMember_whenATeamMember() throws Exception {
    mockMvc.perform(
        get("/portal-team-management/teams/{resId}/member/{personId}/remove",
            regulatorTeam.getId(),
            regulatorTeamAdminPerson.getId().asInt())
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isOk());
  }

  @Test
  void handleRemoveTeamMemberSubmit_userCanManageTeam() throws Exception {

    mockMvc.perform(
        post("/portal-team-management/teams/{resId}/member/{personId}/remove"
            , regulatorTeam.getId()
            , regulatorTeamAdminPerson.getId().asInt())
        .with(user((regulatorTeamAdmin)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlTemplate("/portal-team-management/teams/{resId}/member", regulatorTeam.getId()));

    verify(teamManagementService, times(1)).removeTeamMember(regulatorTeamAdminPerson,
        regulatorTeam, regulatorTeamAdmin);
  }

  @Test
  void handleRemoveTeamMemberSubmit_userCannotManageTeam() throws Exception {

    mockMvc.perform(
        post("/portal-team-management/teams/{resId}/member/{personId}/remove"
            , regulatorTeam.getId()
            , regulatorTeamAdminPerson.getId().asInt())
        .with(user(organisationTeamAdmin))
        .with(csrf()))
        .andExpect(status().isForbidden());

    verify(teamManagementService, times(0)).removeTeamMember(any(), any(), any());
  }

  @Test
  void handleRemoveTeamMemberSubmit_userCanManageTeam_andPersonIsLastAdministratorInTeam() throws Exception {

    doThrow(new LastAdministratorException(""))
        .when(teamManagementService).removeTeamMember(
        regulatorTeamAdminPerson,
        regulatorTeam,
        regulatorTeamAdmin
    );

    mockMvc.perform(
        post("/portal-team-management/teams/{resId}/member/{personId}/remove"
            , regulatorTeam.getId()
            , regulatorTeamAdminPerson.getId().asInt())
        .with(user((regulatorTeamAdmin)))
        .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  void renderAddUserToTeam() throws Exception {
    mockMvc.perform(get("/portal-team-management/teams/{resId}/member/new", regulatorTeam.getId())
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isOk());
  }

  @Test
  void renderAddUserToTeam_whenTeamDoesNotExist() throws Exception {
    mockMvc.perform(get("/portal-team-management/teams/{resId}/member/new", UNKNOWN_RES_ID)
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderAddUserToTeam_whenTeamExists_andUserCannotManageTeam() throws Exception {
    mockMvc.perform(get("/portal-team-management/teams/{resId}/member/new", regulatorTeam.getId())
        .with(user(organisationTeamAdmin)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderAddUserToTeam_whenTeamExists_andUserCanManageTeam() throws Exception {
    mockMvc.perform(get("/portal-team-management/teams/{resId}/member/new", regulatorTeam.getId())
        .with(user(regulatorTeamAdmin)))
        .andExpect(status().isOk());
  }

  @Test
  void handleAddUserToTeamSubmit_whenTeamExists_andUserCanManageTeam_andFormIsValid() throws Exception {
    when(teamManagementService.getPersonByEmailAddressOrLoginId(organisationTeamAdminPerson.getEmailAddress()))
        .thenReturn(Optional.of(organisationTeamAdminPerson));

    mockMvc.perform(post("/portal-team-management/teams/{resId}/member/new", regulatorTeam.getId())
        .with(user(regulatorTeamAdmin))
        .with(csrf())
        .param("userIdentifier", organisationTeamAdminPerson.getEmailAddress()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlTemplate("/portal-team-management/teams/{resId}/member/{personId}/roles",
            regulatorTeam.getId(),
            organisationTeamAdminPerson.getId().asInt()));
  }

  @Test
  void handleAddUserToTeamSubmit_whenTeamExists_andUserCanManageTeam_andFormIsValid_andFormEmailNotKnown() throws Exception {
    mockMvc.perform(post("/portal-team-management/teams/{resId}/member/new", regulatorTeam.getId())
        .with(user(regulatorTeamAdmin))
        .with(csrf())
        .param("userIdentifier", "Some.Unknown@email.com"))
        .andExpect(status().isNotFound());
  }

  @Test
  void handleAddUserToTeamSubmit_whenTeamExists_andUserCanManageTeam_andFormIsInvalid() throws Exception {

    ControllerTestUtils.mockValidatorErrors(addUserToTeamFormValidator, List.of("userIdentifier"));

    mockMvc.perform(
        post("/portal-team-management/teams/{resId}/member/new", regulatorTeam.getId())
            .with(user(regulatorTeamAdmin))
            .with(csrf())
            .param("userIdentifier", ""))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("form"));
  }

  @Test
  void handleAddUserToTeamSubmit_whenTeamExists_andUserCannotManageTeam() throws Exception {
    mockMvc.perform(
        post("/portal-team-management/teams/{resId}/member/new", regulatorTeam.getId())
            .with(user(organisationTeamAdmin))
            .with(csrf())
            .param("newUser", ""))
        .andExpect(status().isForbidden());
  }

}