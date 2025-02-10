package uk.co.ogauthority.pwa.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TestAuthenticationController.class)
@ContextConfiguration(classes = {
    TestAuthenticationController.class
})
public class TestAuthenticationControllerTestResolver extends ResolverAbstractControllerTest {
  private static final String SAML_LOGIN_REDIRECT_URL = "http://localhost/saml2/authenticate/saml";


  @Before
  public void setUp() {
    // needs to be BeforeEach as otherwise MockBean in AbstractControllerTest is not initialised yet
  }

  @Test
  public void whenNoUser_thenVerifyAuthenticationRequired() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(TestAuthenticationController.class).requiresUserEndpoint(null))))
          .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
              .isEqualTo(SAML_LOGIN_REDIRECT_URL));
  }

  @Test
  public void whenUser_thenVerifyAuthorised() throws Exception {

    var expectedUser = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(TestAuthenticationController.class).requiresUserEndpoint(null)))
                .with(user(expectedUser))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    assertThat((AuthenticatedUserAccount) modelAndView.getModel().get("user"))
        .extracting(WebUserAccount::getWuaId)
        .isEqualTo(expectedUser.getWuaId());
  }

}