package uk.co.ogauthority.pwa.teams.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.auth.EnergyPortalConfiguration;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.form.AddMemberFormValidator;
import uk.co.ogauthority.pwa.teams.management.form.MemberRolesFormValidator;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.teams.management.view.TeamTypeView;
import uk.co.ogauthority.pwa.teams.management.view.TeamView;

@SuppressWarnings({"unchecked", "DataFlowIssue"})
@WebMvcTest(TeamManagementController.class)
@Import(PwaMvcTestConfiguration.class)
public class TeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private MemberRolesFormValidator memberRolesFormValidator;

  @MockBean
  private AddMemberFormValidator addMemberFormValidator;

  @MockBean
  private EnergyPortalConfiguration energyPortalConfiguration;

  private static Team regTeam;
  private static Team organisationTeam;
  private static TeamMemberView regTeamMemberView;
  private static AuthenticatedUserAccount invokingUser;

  @BeforeAll
  public static void setUp() {
    regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeam.setName("reg team one");

    organisationTeam = new Team(UUID.randomUUID());
    organisationTeam.setTeamType(TeamType.ORGANISATION);
    organisationTeam.setName("org team");

    regTeamMemberView = new TeamMemberView(
        1L,
        "Ms",
        "Test",
        "User",
        "test@example.com",
        "020123456",
        regTeam.getId(),
        List.of(Role.TEAM_ADMINISTRATOR)
    );

    invokingUser = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();
  }

  @Test
  public void renderTeamTypeList() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser.getWuaId()))
        .thenReturn(Set.of(TeamType.ORGANISATION, TeamType.REGULATOR));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamTypeView>) modelAndView.getModel().get("teamTypeViews");

    assertThat(teamTypeViews)
        .extracting(TeamTypeView::teamTypeName)
        .containsExactly(TeamType.ORGANISATION.getDisplayName(), TeamType.REGULATOR.getDisplayName());
  }

  @Test
  public void renderTeamTypeList_singeTypeRedirects() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser.getWuaId()))
        .thenReturn(Set.of(TeamType.ORGANISATION));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.ORGANISATION.getUrlSlug(), null))));
  }

  @Test
  public void renderTeamTypeList_regWithOrgManageCanSeeOrgTeams() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser.getWuaId()))
        .thenReturn(Set.of(TeamType.REGULATOR));

    when(teamQueryService.userHasStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamTypeView>) modelAndView.getModel().get("teamTypeViews");

    assertThat(teamTypeViews)
        .extracting(TeamTypeView::teamTypeName)
        .containsExactly(TeamType.ORGANISATION.getDisplayName(), TeamType.REGULATOR.getDisplayName());
  }

  @Test
  public void renderTeamTypeList_regWithCgManageCanSeeCgTeams() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser.getWuaId()))
        .thenReturn(Set.of(TeamType.REGULATOR));

    when(teamQueryService.userHasStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamTypeView>) modelAndView.getModel().get("teamTypeViews");

    assertThat(teamTypeViews)
        .extracting(TeamTypeView::teamTypeName)
        .containsExactly(TeamType.CONSULTEE.getDisplayName(), TeamType.REGULATOR.getDisplayName());
  }

  @Test
  public void renderTeamTypeList_noManageableTeams() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser.getWuaId()))
        .thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderTeamsOfType_staticTeamRedirectsToSingleInstance() throws Exception {
    when(teamManagementService.getStaticTeamOfTypeUserIsMemberOf(TeamType.REGULATOR, (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.REGULATOR.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null))));
  }

  @Test
  public void renderTeamsOfType_singleScopedTeamRedirectsToInstance() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION, (long) invokingUser.getWuaId()))
        .thenReturn(Set.of(organisationTeam));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.ORGANISATION.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(organisationTeam.getId(), null))));
  }

  @Test
  public void renderTeamsOfType_scopedTeamReturnList() throws Exception {

    var firstOrganisationTeamByName = new Team(UUID.randomUUID());
    firstOrganisationTeamByName.setTeamType(TeamType.ORGANISATION);
    firstOrganisationTeamByName.setName("a team name");

    var secondOrganisationTeamByName = new Team(UUID.randomUUID());
    secondOrganisationTeamByName.setTeamType(TeamType.ORGANISATION);
    secondOrganisationTeamByName.setName("b team name");

    var thirdOrganisationTeamByName = new Team(UUID.randomUUID());
    thirdOrganisationTeamByName.setTeamType(TeamType.ORGANISATION);
    thirdOrganisationTeamByName.setName("C team name");

    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION, (long) invokingUser.getWuaId()))
        .thenReturn(Set.of(secondOrganisationTeamByName, thirdOrganisationTeamByName, firstOrganisationTeamByName));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class)
        .renderTeamsOfType(TeamType.ORGANISATION.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamView>) modelAndView.getModel().get("teamViews");

    assertThat(teamTypeViews)
        .extracting(TeamView::teamName)
        .containsExactly(
            firstOrganisationTeamByName.getName(),
            secondOrganisationTeamByName.getName(),
            thirdOrganisationTeamByName.getName()
        );
  }

  @Test
  public void renderTeamsOfType_noManageableTeams() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION, (long) invokingUser.getWuaId()))
        .thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.ORGANISATION.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderTeamsOfType_noManageableTeams_orgAdminNotForbidden() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION, (long) invokingUser.getWuaId()))
        .thenReturn(Set.of());

    when(teamQueryService.userHasAtLeastOneStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR,
        Set.of(Role.ORGANISATION_MANAGER, Role.CONSULTEE_GROUP_MANAGER)))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class)
        .renderTeamsOfType(TeamType.ORGANISATION.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var createNewInstanceUrl = (String) modelAndView.getModel().get("createNewInstanceUrl");

    assertThat(createNewInstanceUrl)
        .isEqualTo(TeamType.ORGANISATION.getCreateNewInstanceRoute());
  }

  @Test
  public void renderTeamsOfType_noManageableTeams_cgAdminNotForbidden() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.CONSULTEE, (long) invokingUser.getWuaId()))
        .thenReturn(Set.of());

    when(teamQueryService.userHasAtLeastOneStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR,
        Set.of(Role.ORGANISATION_MANAGER, Role.CONSULTEE_GROUP_MANAGER)))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class)
        .renderTeamsOfType(TeamType.CONSULTEE.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var createNewInstanceUrl = (String) modelAndView.getModel().get("createNewInstanceUrl");

    assertThat(createNewInstanceUrl)
        .isEqualTo(TeamType.CONSULTEE.getCreateNewInstanceRoute());
  }

  @Test
  public void renderTeamMemberList_whenNotMemberOfTeam_thenForbidden() throws Exception {

    var team = regTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, invokingUser.getWuaId()))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderTeamMemberList_whenMemberOfTeam_thenOk() throws Exception {

    var team = regTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, invokingUser.getWuaId()))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk());
  }

  @Test
  public void renderTeamMemberList_whenOrganisationTeam_andNotMemberOfTeam_andUserHasManageAnyOrganisationRole_thenOk() throws Exception {

    // GIVEN an organisation team
    var team = organisationTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    // AND the invoking user is not a direct member
    when(teamManagementService.isMemberOfTeam(team, invokingUser.getWuaId()))
        .thenReturn(false);

    // WHEN the invoking user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM in the regulator team
    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser.getWuaId()))
        .thenReturn(true);

    // THEN the invoking user will be able to view the team
    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk());
  }

  @Test
  public void renderTeamMemberList_whenOrganisationTeam_andNotMemberOfTeam_andUserWithoutManageAnyOrganisationRole_thenForbidden() throws Exception {

    // GIVEN an organisation team
    var team = organisationTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    // AND the invoking user is not a direct member
    when(teamManagementService.isMemberOfTeam(team, invokingUser.getWuaId()))
        .thenReturn(false);

    // WHEN the invoking user does not have the CREATE_MANAGE_ANY_ORGANISATION_TEAM in the regulator team
    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser.getWuaId()))
        .thenReturn(false);

    // THEN the invoking user will not be able to view the team
    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderTeamMemberList_whenNotOrganisationTeam_andNotMemberOfTeam_andCanManageAnyOrganisationRole_thenForbidden() {

    Stream.of(TeamType.values())
        .filter(teamType -> teamType != TeamType.ORGANISATION)
        .forEach(nonOrganisationTeamType -> {
          // GIVEN an non-organisation team
          var team = new Team(UUID.randomUUID());
          team.setTeamType(nonOrganisationTeamType);

          when(teamManagementService.getTeam(team.getId()))
              .thenReturn(Optional.of(team));

          // AND the invoking user is not a direct member
          when(teamManagementService.isMemberOfTeam(team, invokingUser.getWuaId()))
              .thenReturn(false);

          // WHEN the invoking user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM in the regulator team
          when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser.getWuaId()))
              .thenReturn(true);

          // THEN the invoking user will not be able to view the team
          try {
            mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
                    .with(user(invokingUser)))
                .andExpect(status().isForbidden());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Test
  public void renderTeamMemberList_whenIsMemberOfTeamAndTeamManager_thenAssetModelProperties() throws Exception {

    when(teamManagementService.canManageTeam(regTeam, invokingUser.getWuaId()))
        .thenReturn(true);

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.isMemberOfTeam(regTeam, invokingUser.getWuaId()))
        .thenReturn(true);

    when(teamManagementService.getTeamMemberViewsForTeam(regTeam))
        .thenReturn(List.of(regTeamMemberView));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("teamManagement/teamMembers"))
        .andExpect(model().attribute("teamName", regTeam.getName()))
        .andExpect(model().attribute("teamMemberViews", List.of(regTeamMemberView)))
        .andExpect(model().attribute("canManageTeam", true))
        .andExpect(model().attribute(
            "addMemberUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(regTeam.getId(), null))
        ))
        .andExpect(model().attribute("rolesInTeam", regTeam.getTeamType().getAllowedRoles()));
  }

  @Test
  public void renderTeamMemberList_whenIsMemberOfTeamAndNotTeamManager_thenAssetModelProperties() throws Exception {

    when(teamManagementService.canManageTeam(regTeam, invokingUser.getWuaId()))
        .thenReturn(false);

    when(teamManagementService.isMemberOfTeam(regTeam, invokingUser.getWuaId()))
        .thenReturn(true);

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getTeamMemberViewsForTeam(regTeam))
        .thenReturn(List.of(regTeamMemberView));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("teamManagement/teamMembers"))
        .andExpect(model().attribute("teamName", regTeam.getName()))
        .andExpect(model().attribute("teamMemberViews", List.of(regTeamMemberView)))
        .andExpect(model().attribute("canManageTeam", false))
        .andExpect(model().attribute(
            "addMemberUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(regTeam.getId(), null))
        ));
  }

  @Test
  public void renderTeamMemberList_noTeamFound() throws Exception {
    var nonExistentTeamId = UUID.randomUUID();
    when(teamManagementService.getTeam(nonExistentTeamId))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(nonExistentTeamId, null)))
        .with(user(invokingUser)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void renderTeamMemberList_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderAddMemberToTeam() throws Exception {
    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION, (long) invokingUser.getWuaId()))
        .thenReturn(List.of(organisationTeam));

    when(energyPortalConfiguration.registrationUrl())
        .thenReturn("https://example.com");

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(organisationTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var registerUrl = (String) modelAndView.getModel().get("registerUrl");

    assertThat(registerUrl)
        .isEqualTo("https://example.com");
  }

  @Test
  public void renderAddMemberToTeam_noTeamFound() throws Exception {
    var nonExistentTeamId = UUID.randomUUID();
    when(teamManagementService.getTeam(nonExistentTeamId))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(nonExistentTeamId, null)))
        .with(user(invokingUser)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void renderAddMemberToTeam_noAccess() throws Exception {
    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION, (long) invokingUser.getWuaId()))
        .thenReturn(List.of());

    when(energyPortalConfiguration.registrationUrl())
        .thenReturn("https://example.com");

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(organisationTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void handleAddMemberToTeam() throws Exception {
    var epaUser = new User.Builder()
        .webUserAccountId(999)
        .isAccountShared(false)
        .canLogin(true)
        .build();

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    when(addMemberFormValidator.isValid(any(), any(), any()))
        .thenReturn(true);

    when(teamManagementService.getEnergyPortalUser("foo"))
        .thenReturn(List.of(epaUser));

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("username", "foo"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(regTeam.getId(), 999L, null))));
  }

  @Test
  public void handleAddMemberToTeam_invalidForm() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    when(addMemberFormValidator.isValid(any(), any(), any()))
        .thenReturn(false);

    when(energyPortalConfiguration.registrationUrl())
        .thenReturn("https://example.com");

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser)))
        .andExpect(status().isOk()); // No redirect to next page
  }

  @Test
  public void handleAddMemberToTeam_invalidUser() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    when(addMemberFormValidator.isValid(any(), any(), any()))
        .thenReturn(true);

    when(teamManagementService.getEnergyPortalUser("foo"))
        .thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("username", "foo"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void handleAddMemberToTeam_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("username", "foo"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderUserTeamRoles() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getTeamMemberView(regTeam, 999L))
        .thenReturn(regTeamMemberView);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(regTeam.getId(), 999L, null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var roleMap = (Map<String, String>) modelAndView.getModel().get("rolesNamesMap");

    assertThat(roleMap)
        .contains(
            Map.entry(Role.AS_BUILT_NOTIFICATION_ADMIN.name(), Role.AS_BUILT_NOTIFICATION_ADMIN.getName()),
            Map.entry(Role.CASE_OFFICER.name(), Role.CASE_OFFICER.getName()),
            Map.entry(Role.CONSENT_VIEWER.name(), Role.CONSENT_VIEWER.getName()),
            Map.entry(Role.ORGANISATION_MANAGER.name(), Role.ORGANISATION_MANAGER.getName()),
            Map.entry(Role.CONSULTEE_GROUP_MANAGER.name(), Role.CONSULTEE_GROUP_MANAGER.getName()),
            Map.entry(Role.PWA_MANAGER.name(), Role.PWA_MANAGER.getName()),
            Map.entry(Role.TEAM_ADMINISTRATOR.name(), Role.TEAM_ADMINISTRATOR.getName()),
            Map.entry(Role.TEMPLATE_CLAUSE_MANAGER.name(), Role.TEMPLATE_CLAUSE_MANAGER.getName())
        );

    var teamMemberViewModel = (TeamMemberView) modelAndView.getModel().get("teamMemberView");
    assertThat(teamMemberViewModel).isEqualTo(regTeamMemberView);

    var rolesInTeam = (List<Role>) modelAndView.getModel().get("rolesInTeam");

    assertThat(rolesInTeam)
        .contains(
            Role.PWA_MANAGER,
            Role.CASE_OFFICER,
            Role.AS_BUILT_NOTIFICATION_ADMIN,
            Role.TEMPLATE_CLAUSE_MANAGER,
            Role.ORGANISATION_MANAGER,
            Role.CONSULTEE_GROUP_MANAGER,
            Role.CONSENT_VIEWER,
            Role.TEAM_ADMINISTRATOR
        );
  }

  @Test
  public void renderUserTeamRoles_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.empty());


    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(regTeam.getId(), 999L, null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void updateUserTeamRoles() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    when(memberRolesFormValidator.isValid(any(), eq(999L), eq(regTeam), any()))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).updateUserTeamRoles(regTeam.getId(), 999L, null, invokingUser, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("roles", "TEAM_ADMINISTRATOR"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null))));

    verify(teamManagementService).setUserTeamRoles(999L, regTeam, List.of(Role.TEAM_ADMINISTRATOR), (long) invokingUser.getWuaId());
  }

  @Test
  public void updateUserTeamRoles_invalidForm() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    when(memberRolesFormValidator.isValid(any(), eq(999L), eq(regTeam), any()))
        .thenReturn(false);

    when(teamManagementService.getTeamMemberView(regTeam, 999L))
        .thenReturn(regTeamMemberView);

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).updateUserTeamRoles(regTeam.getId(), 999L, null, invokingUser, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("roles", "MANAGE_TEAM"))
        .andExpect(status().isOk()); // No redirect to next page

    verify(teamManagementService, never()).setUserTeamRoles(any(), any(), any(), any());
  }

  @Test
  public void updateUserTeamRoles_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).updateUserTeamRoles(regTeam.getId(), 999L, null, invokingUser, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("roles", "MANAGE_TEAM"))
        .andExpect(status().isForbidden());

    verify(teamManagementService, never()).setUserTeamRoles(any(), any(), any(), any());
  }

  @Test
  public void renderRemoveTeamMember() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getTeamMemberView(regTeam, 999L))
        .thenReturn(regTeamMemberView);

    when(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, 999L))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderRemoveTeamMember(regTeam.getId(), 999L)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamMemberViewModel = (TeamMemberView) modelAndView.getModel().get("teamMemberView");
    var teamName = (String) modelAndView.getModel().get("teamName");
    var canRemoveTeamMember = (boolean) modelAndView.getModel().get("canRemoveTeamMember");

    assertThat(teamMemberViewModel).isEqualTo(regTeamMemberView);
    assertThat(teamName).isEqualTo(regTeam.getName());
    assertThat(canRemoveTeamMember).isTrue();
  }

  @Test
  public void renderRemoveTeamMember_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderRemoveTeamMember(regTeam.getId(), 999L)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void handleRemoveTeamMember() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleRemoveTeamMember(regTeam.getId(), 999L)))
        .with(csrf())
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null))));

    verify(teamManagementService).removeUserFromTeam(999L, regTeam);
  }

  @Test
  public void handleRemoveTeamMember_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleRemoveTeamMember(regTeam.getId(), 999L)))
        .with(csrf())
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());

    verify(teamManagementService, never()).removeUserFromTeam(999L, regTeam);
  }

}
