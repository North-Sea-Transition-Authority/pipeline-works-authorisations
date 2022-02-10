package uk.co.ogauthority.pwa.exception;

import javax.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "The application could not be withdrawn")
public class WithdrawApplicationException extends EntityNotFoundException {
  public WithdrawApplicationException(String message) {
    super(message);
  }
}
