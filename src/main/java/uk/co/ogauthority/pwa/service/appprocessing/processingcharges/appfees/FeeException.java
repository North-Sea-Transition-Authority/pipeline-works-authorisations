package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Unexpected error occurred while processing fees")
public class FeeException extends RuntimeException {

  public FeeException(String message) {
    super(message);
  }
}
