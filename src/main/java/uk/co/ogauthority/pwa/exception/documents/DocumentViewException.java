package uk.co.ogauthority.pwa.exception.documents;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "A document view creation error occurred")
public class DocumentViewException extends RuntimeException {

  public DocumentViewException(String message) {
    super(message);
  }

  public DocumentViewException(String message, Throwable cause) {
    super(message, cause);
  }

}
