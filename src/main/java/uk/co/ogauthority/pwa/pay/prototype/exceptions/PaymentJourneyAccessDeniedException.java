package uk.co.ogauthority.pwa.pay.prototype.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "User does not have access to the requested payment journey")
public class PaymentJourneyAccessDeniedException extends RuntimeException {

  public PaymentJourneyAccessDeniedException(String message) {
    super(message);
  }

}