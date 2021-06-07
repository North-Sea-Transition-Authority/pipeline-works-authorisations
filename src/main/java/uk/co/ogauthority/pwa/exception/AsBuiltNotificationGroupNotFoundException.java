package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND,
    reason = "No as-built notification was found")
public class AsBuiltNotificationGroupNotFoundException extends RuntimeException {

  public AsBuiltNotificationGroupNotFoundException(String message) {
    super(message);
  }
}