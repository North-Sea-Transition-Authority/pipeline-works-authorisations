package uk.co.ogauthority.pwa.features.termsandconditions.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.controller.WithDefaultPageControllerAdvice;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsFilterForm;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@WebMvcTest(TermsAndConditionsManagementController.class)
@ContextConfiguration(classes = TermsAndConditionsManagementController.class)
@WithDefaultPageControllerAdvice
class TermsAndConditionsManagementControllerTest  extends ResolverAbstractControllerTest {

  @MockBean
  TermsAndConditionsService termsAndConditionsService;

  @MockBean
  ControllerHelperService controllerHelperService;

  private AuthenticatedUserAccount userAccount;
  private AuthenticatedUserAccount userAccountNoAuth;

  @BeforeEach
  void setup() {
    userAccount = new AuthenticatedUserAccount(new WebUserAccount(1, new Person()), Set.of(PwaUserPrivilege.PWA_ACCESS));

    userAccountNoAuth = new AuthenticatedUserAccount(new WebUserAccount(2, new Person()), Set.of(PwaUserPrivilege.PWA_ACCESS));

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(userAccount, Map.of(TeamType.REGULATOR, Set.of(Role.PWA_MANAGER))))
        .thenReturn(true);
  }

  @Test
  void renderTermsAndConditionsManagement() throws Exception {

    when(termsAndConditionsService.getPwaManagementScreenPageView(0, ""))
        .thenReturn(new PageView<>(0, 1, List.of(), null, 2, 10));

    var mvc = mockMvc.perform(get(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .renderTermsAndConditionsManagement(null, null, userAccount)))
            .with(user(userAccount)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(mvc.get("termsAndConditionsFormUrl")).isEqualTo(ReverseRouter.route(on(TermsAndConditionsFormController.class)
        .renderNewTermsAndConditionsForm(null, userAccount)));
  }

  @Test
  void renderTermsAndConditionsVariationForm_unauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .renderTermsAndConditionsManagement(null, null, userAccount)))
            .with(user(userAccountNoAuth)))
        .andExpect(status().isForbidden());
  }

  @Test
  void submitTermsAndConditionsVariationForm_post() throws Exception {
    var form = new TermsAndConditionsFilterForm().setPwaReference("1/W/23");

    when(termsAndConditionsService.getPwaManagementScreenPageView(0, "1/W/23"))
        .thenReturn(new PageView<>(0, 1, List.of(), null, 2, 10));

    mockMvc.perform(post(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .filterTermsAndConditions(form, null, userAccount)))
            .with(user(userAccount))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void submitTermsAndConditionsVariationForm_post_unauthenticated() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .filterTermsAndConditions(null, null , userAccountNoAuth)))
            .with(user(userAccountNoAuth)))
        .andExpect(status().isForbidden());
  }
}