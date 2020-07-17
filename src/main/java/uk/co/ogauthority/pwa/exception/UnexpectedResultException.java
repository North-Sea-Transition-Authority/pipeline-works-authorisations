package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "An unexpected result was returned")
public class UnexpectedResultException extends RuntimeException {

  public UnexpectedResultException(String message) {
    super(message);
  }

}