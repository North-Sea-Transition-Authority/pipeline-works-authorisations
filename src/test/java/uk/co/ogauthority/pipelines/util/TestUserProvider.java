package uk.co.ogauthority.pipelines.util;

import javax.servlet.http.Cookie;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import uk.co.ogauthority.pipelines.auth.AuthenticatedUserToken;
import uk.co.ogauthority.pipelines.auth.FoxSessionFilter;
import uk.co.ogauthority.pipelines.model.entity.UserAccount;

public class TestUserProvider implements RequestPostProcessor {

  private final UserAccount authenticatedUser;

  public static TestUserProvider authenticatedUserAndSession(UserAccount authenticatedUser) {
    return new TestUserProvider(authenticatedUser);
  }

  private TestUserProvider(UserAccount authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {

    var sessionId = "session_" + authenticatedUser.getId();
    AuthenticatedUserToken authenticatedUserToken = AuthenticatedUserToken.create(sessionId, authenticatedUser);

    // Set the authentication token on the SecurityContext
    SecurityMockMvcRequestPostProcessors.authentication(authenticatedUserToken).postProcessRequest(request);

    // Add the fox session cookie to the request so FoxSessionFilter doesn't clear the context
    Cookie foxSessionCookie = new Cookie(FoxSessionFilter.SESSION_COOKIE_NAME, sessionId);
    request.setCookies(ArrayUtils.add(request.getCookies(), foxSessionCookie));

    return request;
  }
}