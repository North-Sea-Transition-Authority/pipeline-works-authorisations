package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR,
    reason = "At least one entity must exist in order to find the latest version")
public class EntityLatestVersionNotFoundException extends RuntimeException {

  public EntityLatestVersionNotFoundException(String message) {
    super(message);
  }
}
