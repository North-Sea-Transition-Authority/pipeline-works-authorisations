package uk.co.ogauthority.pwa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error creating temporary file from uploaded file")
public class TempFileException extends RuntimeException {

  public TempFileException(String message) {
    super(message);
  }

  public TempFileException(String message, Throwable cause) {
    super(message, cause);
  }

}
