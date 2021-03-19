package uk.co.ogauthority.pwa.service.pwaapplications.routing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Could not route user to application landing page")
public class ApplicationLandingPageException extends RuntimeException {

  public ApplicationLandingPageException(String message) {
    super(message);
  }
}
