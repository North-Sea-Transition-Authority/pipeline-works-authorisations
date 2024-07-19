package uk.co.ogauthority.pwa.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserToken;
import uk.co.ogauthority.pwa.service.UserSessionService;
import uk.co.ogauthority.pwa.util.SessionUtils;

@Component
public class UserPrivReloadInterceptor implements HandlerInterceptor {

  private final UserSessionService userSessionService;

  @Autowired
  public UserPrivReloadInterceptor(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  /**
   * Want to reload user privs before the request itself is handled as it may impact what actual handler does.
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

    if (existingAuth.getPrincipal() instanceof AuthenticatedUserAccount) {

      AuthenticatedUserAccount authenticatedUser = (AuthenticatedUserAccount) existingAuth.getPrincipal();
      userSessionService.populateUserPrivileges(authenticatedUser);

      // use existing session id to ensure we don't make FoxSessionFilter think the auth has changed
      String sessionId = SessionUtils.getSessionId();
      AuthenticatedUserToken newAuth = AuthenticatedUserToken.create(sessionId, authenticatedUser);

      SecurityContextHolder.getContext().setAuthentication(newAuth);

    }

    return true;

  }

}
