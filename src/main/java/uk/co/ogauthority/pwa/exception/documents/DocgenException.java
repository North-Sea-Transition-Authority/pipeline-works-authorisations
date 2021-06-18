package uk.co.ogauthority.pwa.exception.documents;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "A docgen error occurred")
public class DocgenException extends RuntimeException {

  public DocgenException(String message) {
    super(message);
  }

  public DocgenException(String message, Throwable cause) {
    super(message, cause);
  }

}
