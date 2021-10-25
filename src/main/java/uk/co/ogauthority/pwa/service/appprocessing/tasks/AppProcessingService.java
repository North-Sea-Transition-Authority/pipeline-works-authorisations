package uk.co.ogauthority.pwa.service.appprocessing.tasks;

import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;

/**
 * Interface to be used by all services implementing app processing task behaviour.
 */
public interface AppProcessingService {

  boolean canShowInTaskList(PwaAppProcessingContext processingContext);

  default TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        false,
        task.getDisplayOrder());

  }

}
