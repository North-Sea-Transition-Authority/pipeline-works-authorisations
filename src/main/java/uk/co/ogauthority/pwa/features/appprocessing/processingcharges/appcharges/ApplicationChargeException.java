package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error occurred while processing app charge request")
public class ApplicationChargeException extends RuntimeException {

  public ApplicationChargeException(String message) {
    super(message);
  }
}
