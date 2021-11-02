package uk.co.ogauthority.pwa.integrations.energyportal.teams.external;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This class intercepts 404 errors for non-existent teams and throws an exception which Spring Boot uses to display the correct error page.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Portal team not found")
public class PortalTeamNotFoundException extends RuntimeException {

  public PortalTeamNotFoundException(String message) {
    super(message);
  }
}
