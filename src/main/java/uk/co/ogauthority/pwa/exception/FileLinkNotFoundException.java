package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND,
    reason = "No file link was found")
public class FileLinkNotFoundException extends RuntimeException {

  public FileLinkNotFoundException(String message) {
    super(message);
  }
}