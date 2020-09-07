package uk.co.ogauthority.pwa.service.appprocessing.tasks;

import java.util.Optional;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;

/**
 * Interface to be used by all services implementing app processing task behaviour.
 */
public interface AppProcessingService {

  boolean canShowInTaskList(PwaAppProcessingContext processingContext);

  default Optional<TaskStatus> getTaskStatus(PwaAppProcessingContext processingContext) {
    return Optional.empty();
  }

}
