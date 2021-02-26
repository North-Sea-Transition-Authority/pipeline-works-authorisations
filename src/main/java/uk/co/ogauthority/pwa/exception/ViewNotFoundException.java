package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR,
    reason = "A view object could not be found")
public class ViewNotFoundException extends RuntimeException {

  public ViewNotFoundException(String message) {
    super(message);
  }
}
