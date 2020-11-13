package uk.co.ogauthority.pwa.service.appprocessing.options;

import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission.APPROVE_OPTIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

@Service
public class ApproveOptionsService implements AppProcessingService {

  private final ConsultationRequestService consultationRequestService;

  @Autowired
  public ApproveOptionsService(ConsultationRequestService consultationRequestService) {
    this.consultationRequestService = consultationRequestService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext pwaAppProcessingContext) {
    return hasAccessPermissions(pwaAppProcessingContext);
  }

  private boolean hasAccessPermissions(PwaAppProcessingContext pwaAppProcessingContext) {
    return pwaAppProcessingContext.getAppProcessingPermissions().contains(APPROVE_OPTIONS);
  }

  public boolean taskAccessible(PwaAppProcessingContext pwaAppProcessingContext) {
    var hasAccessPermission = hasAccessPermissions(pwaAppProcessingContext);

    // dont need to query consultations if we know we dont have basic permission
    if (!hasAccessPermission) {
      return false;
    }

    var appStatusCountView = consultationRequestService.getApplicationConsultationStatusView(
        pwaAppProcessingContext.getPwaApplication()
    );

    var openCount = appStatusCountView.sumFilteredStatusCounts(ConsultationRequestStatus::isRequestOpen);
    var respondedCount = appStatusCountView.getCountOfRequestsWithStatus(ConsultationRequestStatus.RESPONDED);

    return openCount == 0 && respondedCount > 0;

  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    var taskAccessible = taskAccessible(processingContext);

    //TODO PWA-116 : options approval data model.
    var isApproved = false;

    TaskStatus taskStatus;
    if (taskAccessible && isApproved) {
      taskStatus = TaskStatus.COMPLETED;
    } else if (taskAccessible) {
      taskStatus = TaskStatus.NOT_COMPLETED;
    } else {
      taskStatus = TaskStatus.CANNOT_START_YET;
    }

    return new TaskListEntry(
        task.getTaskName(),
        taskAccessible ? task.getRoute(processingContext) : null,
        TaskTag.from(taskStatus),
        task.getDisplayOrder());
  }
}
