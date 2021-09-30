package uk.co.ogauthority.pwa.util;

import org.springframework.security.core.context.SecurityContextHolder;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserToken;

public class SessionUtils {

  private SessionUtils() {
    throw new AssertionError();
  }

  public static String getSessionId() {

    if (SecurityContextHolder.getContext().getAuthentication() instanceof AuthenticatedUserToken) {
      return ((AuthenticatedUserToken) SecurityContextHolder.getContext().getAuthentication()).getSessionId();
    }

    return "";

  }

}
