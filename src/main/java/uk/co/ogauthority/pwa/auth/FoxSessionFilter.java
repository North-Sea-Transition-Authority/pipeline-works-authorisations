package uk.co.ogauthority.pwa.auth;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;
import uk.co.ogauthority.pwa.model.entity.UserSession;
import uk.co.ogauthority.pwa.service.UserSessionService;

public class FoxSessionFilter extends GenericFilterBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(FoxSessionFilter.class);

  public static final String SESSION_COOKIE_NAME = "p_dti_session_id";

  private final UserSessionService userSessionService;
  private final Supplier<SecurityContextRepository> securityContextRepositorySupplier;

  public FoxSessionFilter(UserSessionService userSessionService, Supplier<SecurityContextRepository> securityContextRepositorySupplier) {
    this.userSessionService = userSessionService;
    this.securityContextRepositorySupplier = securityContextRepositorySupplier;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    Cookie foxSessionCookie = WebUtils.getCookie(httpServletRequest, SESSION_COOKIE_NAME);
    checkAuthentication(foxSessionCookie, httpServletRequest.getSession(false), httpServletRequest, httpServletResponse);
    chain.doFilter(request, response);
  }

  @VisibleForTesting
  void checkAuthentication(
      Cookie foxSessionCookie,
      @Nullable HttpSession httpSession,
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    var cookieSessionId = Optional.ofNullable(foxSessionCookie).map(Cookie::getValue).orElse("");

    String cachedSessionId;
    if (SecurityContextHolder.getContext().getAuthentication() instanceof AuthenticatedUserToken) {
      cachedSessionId = ((AuthenticatedUserToken) SecurityContextHolder.getContext().getAuthentication()).getSessionId();
    } else {
      cachedSessionId = "";
    }

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

      // Auth has changed. Clear out any existing session data.
      if (httpSession != null) {
        if (StringUtils.isNotBlank(cachedSessionId)) {
          LOGGER.info("Invalidating HTTP session due to stale cached FOX session ID {}", cachedSessionId);
          httpSession.invalidate();
        } else {
          // Session should not be invalidated if there is no cached session ID. If multiple requests are
          // in-flight from the frontend, invalidating the session here creates a race condition where a session is
          // created and persisted in request 1, but clobbered by request 2 (which clears the session cookie from the
          // browser). Subsequent requests then fail CSRF validation. See SBA-5212
          LOGGER.info("Skipping HTTP session invalidation as no FOX session ID was cached on session");
        }
      }

      SecurityContextHolder.getContext().setAuthentication(authenticatedUserToken);
      securityContextRepositorySupplier.get().saveContext(SecurityContextHolder.getContext(), request, response);
    } else if (optionalUserSession.isEmpty() && StringUtils.isNotBlank(cachedSessionId)) {
      // Session has been invalidated - clear the cached context
      SecurityContextHolder.clearContext();
      if (httpSession != null) {
        httpSession.invalidate();
      }
    }

  }
}
