package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Workflow selected is not in a valid state")
public class WorkflowException extends RuntimeException {

  public WorkflowException(String message) {
    super(message);
  }

  public WorkflowException(String message, Throwable cause) {
    super(message, cause);
  }

}
