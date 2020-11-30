package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error while attempting to submit an application")
public class ApplicationSubmissionException extends RuntimeException  {

  public ApplicationSubmissionException(String message) {
    super(message);
  }

}
