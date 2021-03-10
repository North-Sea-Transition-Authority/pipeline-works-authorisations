package uk.co.ogauthority.pwa.exception.appprocessing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Consent review problem occurred")
public class ConsentReviewException extends RuntimeException {

  public ConsentReviewException(String message) {
    super(message);
  }

}
