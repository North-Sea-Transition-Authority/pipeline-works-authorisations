package uk.co.ogauthority.pwa.mvc;

import com.google.common.annotations.VisibleForTesting;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.util.SecurityUtils;

@Component
public class PostAuthenticationRequestMdcFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PostAuthenticationRequestMdcFilter.class);

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request,
                                  @NotNull HttpServletResponse response,
                                  @NotNull FilterChain filterChain) throws ServletException, IOException {
    try {
      if (isUserLoggedIn()) {
        var authenticatedUser = getAuthenticatedUserAccount();
        MDC.put(RequestLogFilter.MDC_WUA_ID,
            authenticatedUser.map(authenticatedUserAccount -> String.valueOf(authenticatedUserAccount.getWuaId()))
                .orElse(null));
        MDC.put(RequestLogFilter.MDC_REQUEST_TYPE, "authenticated");
      } else {
        MDC.put(RequestLogFilter.MDC_REQUEST_TYPE, "guest");
      }
    } catch (Exception e) {
      LOGGER.error("Error getting user details for MDC", e);
    }

    filterChain.doFilter(request, response);
  }

  @VisibleForTesting
  boolean isUserLoggedIn() {
    return SecurityUtils.isUserLoggedIn();
  }

  @VisibleForTesting
  Optional<AuthenticatedUserAccount> getAuthenticatedUserAccount() {
    return SecurityUtils.getAuthenticatedUserFromSecurityContext();
  }
}
