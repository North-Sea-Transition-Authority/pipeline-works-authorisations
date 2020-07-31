package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error while doing an arbitrary template render")
public class TemplateRenderingException extends RuntimeException  {

  public TemplateRenderingException(String message) {
    super(message);
  }

  public TemplateRenderingException(String message, Throwable cause) {
    super(message, cause);
  }

}
