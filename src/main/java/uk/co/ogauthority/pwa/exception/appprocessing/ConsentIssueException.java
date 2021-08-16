package uk.co.ogauthority.pwa.exception.appprocessing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Consent issue problem occurred")
public class ConsentIssueException extends RuntimeException {

  public ConsentIssueException(String message) {
    super(message);
  }

  public ConsentIssueException(String message, Throwable cause) {
    super(message, cause);
  }

}
