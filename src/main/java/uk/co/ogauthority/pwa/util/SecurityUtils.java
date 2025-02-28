package uk.co.ogauthority.pwa.util;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.saml.ServiceSaml2Authentication;

public class SecurityUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtils.class);

  public static Optional<AuthenticatedUserAccount> getAuthenticatedUserFromSecurityContext() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      LOGGER.debug("SecurityContext contained no Authentication object");
      return Optional.empty();
    }

    if (!(authentication instanceof ServiceSaml2Authentication)) {
      LOGGER.debug("ServiceSaml2Authentication not found in authentication context");
      return Optional.empty();
    }

    if (authentication.getPrincipal() == null) {
      LOGGER.debug("Principal was null when trying to resolve controller argument");
      return Optional.empty();
    }

    if (!(authentication.getPrincipal() instanceof AuthenticatedUserAccount)) {
      LOGGER.debug("Principal was not a AuthenticatedUserAccount when trying to resolve controller argument (was a {})",
          authentication.getPrincipal().getClass());
      return Optional.empty();
    }

    return Optional.of((AuthenticatedUserAccount) authentication.getPrincipal());
  }

  public static boolean isUserLoggedIn() {
    if (SecurityContextHolder.getContext().getAuthentication() instanceof ServiceSaml2Authentication authentication) {
      return authentication.getPrincipal() instanceof AuthenticatedUserAccount;
    }
    return false;
  }

}
