package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "An error occurred trying to copy an entity")
public class EntityCopyingException extends RuntimeException {

  public EntityCopyingException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
