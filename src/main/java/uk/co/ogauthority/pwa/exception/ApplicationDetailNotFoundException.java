package uk.co.ogauthority.pwa.exception;

import javax.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The application detail could not be found")
public class ApplicationDetailNotFoundException extends EntityNotFoundException {
  public ApplicationDetailNotFoundException(String message) {
    super(message);
  }
}
