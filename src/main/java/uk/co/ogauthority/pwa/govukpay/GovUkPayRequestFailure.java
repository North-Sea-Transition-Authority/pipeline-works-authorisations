package uk.co.ogauthority.pwa.govukpay;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception to be used when requests to gov uk pay are not successful.
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error occurred while making request to gov uk pay")
public class GovUkPayRequestFailure extends RuntimeException {

  public GovUkPayRequestFailure(String message) {
    super(message);
  }

}
