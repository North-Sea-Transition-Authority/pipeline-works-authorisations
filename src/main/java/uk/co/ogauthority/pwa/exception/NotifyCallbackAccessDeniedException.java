package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This is thrown whenever an invalid access on NotifyCallback endpoint is attempted.
 * A legitimate access will have to come as a callback request originating from GOV.UK Notify with a valid bearer token.
 * For more information about callbacks visit https://docs.notifications.service.gov.uk/java.html#callbacks.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Access request for NotifyCallback cannot be authorised")
public class NotifyCallbackAccessDeniedException extends RuntimeException {
  public NotifyCallbackAccessDeniedException(String message) {
    super(message);
  }
}
