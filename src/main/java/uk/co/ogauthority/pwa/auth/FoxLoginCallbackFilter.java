package uk.co.ogauthority.pwa.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Re-entry point after a user has logged in using FOX. Piggybacks off the Spring Security functionality to redirect
 * the user to their originally requested URL. This must be a filter instead of a controller as the redirect must be
 * applied before FoxSessionFilter detects the auth change and invalidates the session (which clears the originally
 * requested URL).
 */
@Component
public class FoxLoginCallbackFilter extends GenericFilterBean {

  private final RequestMatcher requestMatcher = new AntPathRequestMatcher("/login/callback");

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;

    if (requestMatcher.matches(request)) {
      SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      handler.onAuthenticationSuccess(request, (HttpServletResponse) servletResponse, authentication);
    } else {
      chain.doFilter(servletRequest, servletResponse);
    }
  }
}

