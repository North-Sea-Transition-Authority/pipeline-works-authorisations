package uk.co.ogauthority.pwa.exception.documents;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "A document instance error occurred")
public class DocumentInstanceException extends RuntimeException {

  public DocumentInstanceException(String message) {
    super(message);
  }

  public DocumentInstanceException(String message, Throwable cause) {
    super(message, cause);
  }

}
