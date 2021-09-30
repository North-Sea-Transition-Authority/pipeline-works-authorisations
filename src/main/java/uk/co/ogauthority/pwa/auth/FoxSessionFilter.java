package uk.co.ogauthority.pwa.auth;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;
import uk.co.ogauthority.pwa.model.entity.UserSession;
import uk.co.ogauthority.pwa.service.UserSessionService;
import uk.co.ogauthority.pwa.util.SessionUtils;

@Component
public class FoxSessionFilter extends GenericFilterBean {

  public static final String SESSION_COOKIE_NAME = "p_dti_session_id";

  private final UserSessionService userSessionService;

  @Autowired
  public FoxSessionFilter(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    Cookie foxSessionCookie = WebUtils.getCookie(httpServletRequest, SESSION_COOKIE_NAME);
    checkAuthentication(foxSessionCookie, httpServletRequest.getSession(false));
    chain.doFilter(request, response);
  }

  @VisibleForTesting
  void checkAuthentication(Cookie foxSessionCookie, @Nullable HttpSession httpSession) {

    String cookieSessionId = Optional.ofNullable(foxSessionCookie).map(Cookie::getValue).orElse("");

    String cachedSessionId = SessionUtils.getSessionId();

    // The authentication has changed and requires a refresh if there was no previously cached session, or if the
    // cookie value has changed between requests
    boolean authenticationChanged = StringUtils.isBlank(cachedSessionId) || !cachedSessionId.equals(cookieSessionId);

    Optional<UserSession> optionalUserSession;
    if (StringUtils.isNotBlank(cookieSessionId)) {
      optionalUserSession = userSessionService.getAndValidateSession(cookieSessionId, authenticationChanged);
    } else {
      optionalUserSession = Optional.empty();
    }

    if (optionalUserSession.isPresent() && authenticationChanged) {

      AuthenticatedUserToken authenticatedUserToken = optionalUserSession
          .map(session -> AuthenticatedUserToken.create(session.getId(), session.getAuthenticatedUserAccount()))
          .get();

      SecurityContextHolder.getContext().setAuthentication(authenticatedUserToken);

      // Auth has changed. Clear out any existing session data.
      if (httpSession != null) {
        httpSession.invalidate();
      }

    } else if (optionalUserSession.isEmpty()) {

      // Session has been invalidated - clear the cached context
      SecurityContextHolder.clearContext();

      if (httpSession != null) {
        httpSession.invalidate();
      }

    }

  }
}
