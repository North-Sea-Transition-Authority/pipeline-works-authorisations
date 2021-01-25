package uk.co.ogauthority.pwa.exception.images;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Problem occurred scaling an uploaded image")
public class ImageScalingException extends RuntimeException {

  public ImageScalingException(String message) {
    super(message);
  }

  public ImageScalingException(String message, Throwable cause) {
    super(message, cause);
  }

}