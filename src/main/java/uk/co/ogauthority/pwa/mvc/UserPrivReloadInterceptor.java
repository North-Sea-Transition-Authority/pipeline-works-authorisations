package uk.co.ogauthority.pwa.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.UserSessionService;

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

      Authentication newAuth = new PreAuthenticatedAuthenticationToken(
          authenticatedUser,
          existingAuth.getCredentials(),
          authenticatedUser.getAuthorities()
      );

      SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
    return true;
  }
}
