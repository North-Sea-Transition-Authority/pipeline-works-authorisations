package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.controller.WithDefaultPageControllerAdvice;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeFormValidator;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@WebMvcTest(controllers = PwaResourceTypeController.class)
@ContextConfiguration(classes = PwaResourceTypeController.class)
@WithDefaultPageControllerAdvice
class PwaResourceTypeControllerTest extends ResolverAbstractControllerTest {

  @MockBean
  PwaResourceTypeFormValidator validator;

  private final AuthenticatedUserAccount permittedUser = new AuthenticatedUserAccount(new WebUserAccount(123),
      Set.of(PwaUserPrivilege.PWA_ACCESS));

  private final AuthenticatedUserAccount prohibitedUser = new AuthenticatedUserAccount(new WebUserAccount(999),
      Set.of(PwaUserPrivilege.PWA_ACCESS));

  @BeforeEach
  void setUp() {
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(permittedUser, Map.of(TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))))
        .thenReturn(true);
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(prohibitedUser, Map.of(TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))))
        .thenReturn(false);
    doCallRealMethod().when(hasTeamRoleService).userHasAnyRoleInTeamType(any(AuthenticatedUserAccount.class),
        eq(TeamType.ORGANISATION), anySet());
  }

  @Test
  void renderResourceScreen_withAuthenticatedUser() throws Exception {
    var resourceOptions = Arrays.stream(PwaResourceType.values())
        .sorted(Comparator.comparingInt(PwaResourceType::getDisplayOrder))
        .collect(Collectors.toList());

    mockMvc.perform(get(ReverseRouter.route(on(PwaResourceTypeController.class)
        .renderResourceTypeForm(null, null)))
        .with(user(permittedUser)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("resourceOptions", resourceOptions));
  }

  @Test
  void renderResourceScreen_noPrivileges() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(PwaResourceTypeController.class)
        .renderResourceTypeForm(null, null)))
        .with(user(prohibitedUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void postResourceScreen_noPrivileges() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(PwaResourceTypeController.class)
        .postResourceType(null, null, null)))
        .with(user(prohibitedUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void postResourceScreen_validHydrogen() throws Exception {
    var form = new PwaResourceTypeForm();
    form.setResourceType(PwaResourceType.HYDROGEN);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    mockMvc.perform(post(ReverseRouter.route(on(PwaResourceTypeController.class)
        .postResourceType(form, bindingResult, null)))
        .with(user(permittedUser))
        .with(csrf())
        .param("resourceType", PwaResourceType.HYDROGEN.name()));
    verify(validator).validate(any(), any());
    verify(pwaApplicationRedirectService).getStartApplicationRedirect(PwaResourceType.HYDROGEN);
  }

  @Test
  void postResourceScreen_validPetroleum() throws Exception {
    var form = new PwaResourceTypeForm();
    form.setResourceType(PwaResourceType.PETROLEUM);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    mockMvc.perform(post(ReverseRouter.route(on(PwaResourceTypeController.class)
        .postResourceType(form, bindingResult, null)))
        .with(user(permittedUser))
        .with(csrf())
        .param("resourceType", PwaResourceType.PETROLEUM.name()));
    verify(validator).validate(any(), any());
    verify(pwaApplicationRedirectService).getStartApplicationRedirect(PwaResourceType.PETROLEUM);
  }

  @Test
  void postResourceScreen_validCCUS() throws Exception {
    var form = new PwaResourceTypeForm();
    form.setResourceType(PwaResourceType.CCUS);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    mockMvc.perform(post(ReverseRouter.route(on(PwaResourceTypeController.class)
        .postResourceType(form, bindingResult, null)))
        .with(user(permittedUser))
        .with(csrf())
        .param("resourceType", PwaResourceType.CCUS.name()));
    verify(validator).validate(any(), any());
    verify(pwaApplicationRedirectService).getStartApplicationRedirect(PwaResourceType.CCUS);
  }
}
