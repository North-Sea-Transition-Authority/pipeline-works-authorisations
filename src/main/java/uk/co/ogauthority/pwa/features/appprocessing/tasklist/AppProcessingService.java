package uk.co.ogauthority.pwa.features.appprocessing.tasklist;

import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;

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
