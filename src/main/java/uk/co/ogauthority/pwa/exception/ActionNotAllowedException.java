package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "A conditional requirement has not been met")
public class ActionNotAllowedException extends RuntimeException {

  public ActionNotAllowedException(String message) {
    super(message);
  }

  public ActionNotAllowedException(String message, Throwable cause) {
    super(message, cause);
  }

}
