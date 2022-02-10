package uk.co.ogauthority.pwa.features.pwapay;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception to be used when PWA payment code encounters an error.
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error occurred while handling payment processing")
public class PwaPaymentsException extends RuntimeException {

  public PwaPaymentsException(String message) {
    super(message);
  }

  public PwaPaymentsException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
