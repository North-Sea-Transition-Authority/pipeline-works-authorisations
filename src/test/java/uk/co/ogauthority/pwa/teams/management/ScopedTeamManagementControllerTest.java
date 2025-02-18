package uk.co.ogauthority.pwa.teams.management;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.form.NewOrganisationTeamFormValidator;

@WebMvcTest(ScopedTeamManagementController.class)
@Import(PwaMvcTestConfiguration.class)
class ScopedTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private OrganisationApi organisationApi;

  @MockBean
  private NewOrganisationTeamFormValidator newOrganisationTeamFormValidator;

  private static AuthenticatedUserAccount invokingUser;

  @BeforeAll
  static void setUp() {
    invokingUser = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();
  }

  @Test
  void renderCreateNewOrgTeam() throws Exception {
    when(teamQueryService.userHasStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewOrgTeam(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk());
  }

  @Test
  void renderCreateNewOrgTeam_noAccess() throws Exception {
    when(teamQueryService.userHasStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewOrgTeam(null)))
            .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void handleCreateNewOrgTeam() throws Exception {
    var orgGroup = new OrganisationGroup();
    orgGroup.setOrganisationGroupId(50);
    orgGroup.setName("Some Org");

    var newTeam = new Team(UUID.randomUUID());

    when(teamQueryService.userHasStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    when(newOrganisationTeamFormValidator.isValid(any(), any()))
        .thenReturn(true);

    when(organisationApi.findOrganisationGroup(eq(50), any(), any()))
        .thenReturn(Optional.of(orgGroup));

    when(teamManagementService.createScopedTeam(eq(orgGroup.getName()), eq(TeamType.ORGANISATION), refEq(TeamScopeReference.from("50", "ORGGRP"))))
        .thenReturn(newTeam);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrgTeam(null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("orgGroupId", "50"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(newTeam.getId(), null))));
  }

  @Test
  void handleCreateNewOrgTeam_invalidForm() throws Exception {
    when(teamQueryService.userHasStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    when(newOrganisationTeamFormValidator.isValid(any(), any()))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrgTeam(null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("orgGroupId", ""))
        .andExpect(status().isOk()); // No redirect to next page

    verify(teamManagementService, never()).createScopedTeam(any(), any(), any());
  }

  @Test
  void handleCreateNewOrgTeam_noAccess() throws Exception {
    when(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrgTeam(null, null)))
            .with(csrf())
            .with(user(invokingUser))
            .param("orgGroupId", ""))
        .andExpect(status().isForbidden()); // No redirect to next page

    verify(teamManagementService, never()).createScopedTeam(any(), any(), any());
  }

  @Test
  void searchOrganisation() throws Exception {
    when(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.ORGANISATION_MANAGER))
        .thenReturn(true);

    var orgGroup1 = new OrganisationGroup();
    orgGroup1.setOrganisationGroupId(1);
    orgGroup1.setName("SHELL one");

    var orgGroup2 = new OrganisationGroup();
    orgGroup2.setOrganisationGroupId(2);
    orgGroup2.setName("SHELL two");

    when(organisationApi.searchOrganisationGroups(eq("shell"), any(), any()))
        .thenReturn(List.of(orgGroup2, orgGroup1));

    mockMvc.perform(get(ReverseRouter.route(on(ScopedTeamManagementController.class).searchOrganisation("shell")))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"SHELL one"}, {"id":"2","text":"SHELL two"}]}
         """));
  }

}
