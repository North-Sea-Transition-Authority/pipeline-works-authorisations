package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error occurred while generating an app summary section")
public class ApplicationSummarisationException extends RuntimeException {

  public ApplicationSummarisationException(String message) {
    super(message);
  }
}
