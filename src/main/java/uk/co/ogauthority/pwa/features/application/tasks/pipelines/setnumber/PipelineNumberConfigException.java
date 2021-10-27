package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Problem retrieving pipeline number config")
public class PipelineNumberConfigException extends RuntimeException {

  PipelineNumberConfigException(String message) {
    super(message);
  }
}
