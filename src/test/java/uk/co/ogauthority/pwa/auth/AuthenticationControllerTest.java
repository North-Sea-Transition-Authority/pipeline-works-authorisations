package uk.co.ogauthority.pwa.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AuthenticationControllerTest.TestAuthenticationController.class)
@ContextConfiguration(classes = {
    AuthenticationControllerTest.TestAuthenticationController.class
})
public class AuthenticationControllerTest extends AbstractControllerTest {
  private static final String SAML_LOGIN_REDIRECT_URL = "http://localhost/saml2/authenticate?registrationId=saml";

  @Controller
  @RequestMapping("/auth")
  public static class TestAuthenticationController {

    @GetMapping("/secured")
    public ModelAndView renderSecured() {
      return new ModelAndView();
    }

  }

  @Test
  public void authenticationRequired() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TestAuthenticationController.class)
            .renderSecured())))
        .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
            .isEqualTo(SAML_LOGIN_REDIRECT_URL));
  }

  @Test
  public void authorisedRequest() throws Exception {
    var user = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();
    mockMvc.perform(get(ReverseRouter.route(on(TestAuthenticationController.class).renderSecured()))
        .with(user(user)))
        .andExpect(status().isOk());
  }
}
