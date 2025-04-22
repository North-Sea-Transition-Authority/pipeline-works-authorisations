package uk.co.ogauthority.pwa.auth;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@WebMvcTest(HasAnyRoleInterceptorTest.TestController.class)
@ContextConfiguration(classes = HasAnyRoleInterceptorTest.TestController.class)
class HasAnyRoleInterceptorTest extends ResolverAbstractControllerTest {
  private static final AuthenticatedUserAccount USER = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();

  @Test
  void preHandle_whenMethodHasNoSupportedAnnotations_thenOkResponse() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).noSupportedAnnotations()))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenUserDoesNotHaveRole_thenForbidden() throws Exception {

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(USER, Map.of(TeamType.ORGANISATION,
        Collections.singleton(Role.APPLICATION_SUBMITTER)))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).withSingleAllowedRole()))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenUserHasRole_thenOk() throws Exception {

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(USER, Map.of(TeamType.REGULATOR,
        Collections.singleton(Role.PWA_MANAGER)))).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).withSingleAllowedRole()))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenUserHasRoleMultipleAllowed_thenOk() throws Exception {

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(USER, Map.of(
        TeamType.REGULATOR, Collections.singleton(Role.PWA_MANAGER),
        TeamType.ORGANISATION, Collections.singleton(Role.APPLICATION_SUBMITTER)))).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).withMultipleAllowedRoles()))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenUserDoesNotHaveRoleMultipleAllowed_thenForbidden() throws Exception {

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(USER, Map.of(
        TeamType.REGULATOR, Collections.singleton(Role.PWA_MANAGER),
        TeamType.ORGANISATION, Collections.singleton(Role.APPLICATION_SUBMITTER)))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).withMultipleAllowedRoles()))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenUserHasRoleMultipleAllowedByGroup_thenOk() throws Exception {

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(USER, RoleGroup.APPLICATION_SEARCH.getRolesByTeamType())).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).withMultipleAllowedRolesViaGroup()))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenUserDoesNotHaveRoleMultipleAllowedByGroup_thenForbidden() throws Exception {

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(USER, RoleGroup.APPLICATION_SEARCH.getRolesByTeamType())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).withMultipleAllowedRolesViaGroup()))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Controller
  static class TestController {

    private static final String VIEW_NAME = "test-view";

    @GetMapping("/no-supported-annotation")
    ModelAndView noSupportedAnnotations() {
      return new ModelAndView(VIEW_NAME);
    }

    @HasAnyRole(roles = Role.PWA_MANAGER, teamType = TeamType.REGULATOR)
    @GetMapping("/with-single-role")
    ModelAndView withSingleAllowedRole() {
      return new ModelAndView(VIEW_NAME);
    }

    @HasAnyRole(roles = Role.PWA_MANAGER, teamType = TeamType.REGULATOR)
    @HasAnyRole(roles = Role.APPLICATION_SUBMITTER, teamType = TeamType.ORGANISATION)
    @GetMapping("/with-multiple-roles")
    ModelAndView withMultipleAllowedRoles() {
      return new ModelAndView(VIEW_NAME);
    }

    @HasAnyRoleByGroup(roleGroup = RoleGroup.APPLICATION_SEARCH)
    @GetMapping("/with-multiple-roles-via-group")
    ModelAndView withMultipleAllowedRolesViaGroup() {
      return new ModelAndView(VIEW_NAME);
    }
  }
}
