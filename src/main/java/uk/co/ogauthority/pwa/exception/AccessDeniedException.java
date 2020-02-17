package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "User does not have access to the requested resource")
public class AccessDeniedException extends RuntimeException {

  public AccessDeniedException(String message) {
    super(message);
  }

}