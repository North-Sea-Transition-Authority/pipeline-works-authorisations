package uk.co.ogauthority.pwa.service.appprocessing;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;

@Service
public class CaseSetupService implements AppProcessingService {

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_OFFICER_REVIEW);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        TaskStatus.NOT_COMPLETED,
        task.getDisplayOrder());

  }
}
