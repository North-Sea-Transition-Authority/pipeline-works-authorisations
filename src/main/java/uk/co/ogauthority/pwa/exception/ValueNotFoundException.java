package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "The value selected is not valid")
public class ValueNotFoundException extends RuntimeException {

  public ValueNotFoundException(String message) {
    super(message);
  }

  public ValueNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}