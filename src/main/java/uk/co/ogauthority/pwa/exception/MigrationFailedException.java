package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Already migrated PWA")
public class MigrationFailedException extends RuntimeException {

  public MigrationFailedException(String message) {
    super(message);
  }

  public MigrationFailedException(String message, Throwable cause) {
    super(message, cause);
  }

}