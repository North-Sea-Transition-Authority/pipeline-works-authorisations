package uk.co.ogauthority.pwa.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Could not authenticate the user")
public class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException(String message) {
    super(message);
  }
}
