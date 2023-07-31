package uk.co.ogauthority.pwa.features.termsandconditions.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsFilterForm;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;

@RunWith(SpringRunner.class)
@WebMvcTest(TermsAndConditionsManagementController.class)
@Import(PwaMvcTestConfiguration.class)
public class TermsAndConditionsManagementControllerTest  extends AbstractControllerTest{

  @MockBean
  TermsAndConditionsService termsAndConditionsService;

  @MockBean
  ControllerHelperService controllerHelperService;

  private AuthenticatedUserAccount userAccount;
  private AuthenticatedUserAccount userAccountNoAuth;

  @Before
  public void setup() {
    userAccount = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()),
        EnumSet.of(PwaUserPrivilege.PWA_MANAGER));

    userAccountNoAuth = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()), Set.of());
  }

  @Test
  public void renderTermsAndConditionsManagement() throws Exception {

    when(termsAndConditionsService.getPwaManagementScreenPageView(0, ""))
        .thenReturn(new PageView<>(0, 1, List.of(), null, 2, 10));

    var mvc = mockMvc.perform(get(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .renderTermsAndConditionsManagement(null, null, userAccount)))
            .with(authenticatedUserAndSession(userAccount)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(mvc.get("termsAndConditionsFormUrl")).isEqualTo(ReverseRouter.route(on(TermsAndConditionsFormController.class)
        .renderTermsAndConditionsVariationForm(null, userAccount)));
  }

  @Test
  public void renderTermsAndConditionsVariationForm_unauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .renderTermsAndConditionsManagement(null, null, userAccount)))
            .with(authenticatedUserAndSession(userAccountNoAuth)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void submitTermsAndConditionsVariationForm_post() throws Exception {
    var form = new TermsAndConditionsFilterForm().setPwaReference("1/W/23");

    when(termsAndConditionsService.getPwaManagementScreenPageView(0, "1/W/23"))
        .thenReturn(new PageView<>(0, 1, List.of(), null, 2, 10));

    mockMvc.perform(post(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .filterTermsAndConditions(form, null, userAccount)))
            .with(authenticatedUserAndSession(userAccount))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void submitTermsAndConditionsVariationForm_post_unauthenticated() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .filterTermsAndConditions(null, null , userAccountNoAuth)))
            .with(authenticatedUserAndSession(userAccountNoAuth)))
        .andExpect(status().isForbidden());
  }
}