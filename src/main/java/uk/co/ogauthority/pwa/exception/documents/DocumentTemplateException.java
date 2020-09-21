package uk.co.ogauthority.pwa.exception.documents;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "A document template error occurred")
public class DocumentTemplateException extends RuntimeException {

  public DocumentTemplateException(String message) {
    super(message);
  }

  public DocumentTemplateException(String message, Throwable cause) {
    super(message, cause);
  }

}
