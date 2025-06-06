package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.routing.ApplicationLandingPageInstance;
import uk.co.ogauthority.pwa.service.pwaapplications.routing.ApplicationLandingPageService;

@WebMvcTest(ApplicationLandingPageRouterController.class)
@Import(PwaMvcTestConfiguration.class)
class ApplicationLandingPageRouterControllerTest extends AbstractControllerTest {

  private static final int APP_ID = 10;
  private static final String ROUTE = "Example/Route";

  private AuthenticatedUserAccount authenticatedUserAccount;

  @MockBean
  private ApplicationLandingPageService applicationLandingPageService;

  @Mock
  private ApplicationLandingPageInstance applicationLandingPageInstance;

  @Test
  void route_serviceInteractions() throws Exception {

    when(applicationLandingPageService.getApplicationLandingPage(any(), anyInt())).thenReturn(
        applicationLandingPageInstance);
    when(applicationLandingPageInstance.getUrl()).thenReturn(ROUTE);

    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(),
        EnumSet.allOf(PwaUserPrivilege.class));

   mockMvc.perform(get(ReverseRouter.route(on(ApplicationLandingPageRouterController.class).route(APP_ID, null)))
        .with(user(authenticatedUserAccount)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ROUTE));

    verify(applicationLandingPageService, times(1)).getApplicationLandingPage(authenticatedUserAccount, APP_ID);

  }
}