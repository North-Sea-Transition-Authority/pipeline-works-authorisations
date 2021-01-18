package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR,
    reason = "Application must be the first version and in DRAFT status in order to be deleted")
public class ApplicationDeletionException extends RuntimeException {

  public ApplicationDeletionException(String message) {
    super(message);
  }
}
