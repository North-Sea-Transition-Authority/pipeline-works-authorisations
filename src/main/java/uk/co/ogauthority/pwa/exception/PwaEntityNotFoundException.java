package uk.co.ogauthority.pwa.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The item could not be found")
public class PwaEntityNotFoundException extends EntityNotFoundException {
  public PwaEntityNotFoundException(String message) {
    super(message);
  }
}
