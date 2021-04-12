package uk.co.ogauthority.pwa.service.appprocessing.options;

import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission.CLOSE_OUT_OPTIONS;
import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Service
public class CloseOutOptionsTaskService implements AppProcessingService {

  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  private final ApproveOptionsService approveOptionsService;

  @Autowired
  public CloseOutOptionsTaskService(ApplicationUpdateRequestService applicationUpdateRequestService,
                                    ApproveOptionsService approveOptionsService) {
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.approveOptionsService = approveOptionsService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext pwaAppProcessingContext) {
    return hasAccessPermissions(pwaAppProcessingContext);
  }

  private boolean hasAccessPermissions(PwaAppProcessingContext pwaAppProcessingContext) {
    return pwaAppProcessingContext.getAppProcessingPermissions().contains(CLOSE_OUT_OPTIONS)
        || (pwaAppProcessingContext.getApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)
        && pwaAppProcessingContext.getAppProcessingPermissions().contains(SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY));
  }

  public boolean taskAccessible(PwaAppProcessingContext pwaAppProcessingContext) {
    var hasAccessPermissions = hasAccessPermissions(pwaAppProcessingContext);

    var taskStatus = getTaskStatus(pwaAppProcessingContext);

    return hasAccessPermissions && taskStatusGrantsTaskAccess(taskStatus, pwaAppProcessingContext.getAppProcessingPermissions());
  }

  private boolean taskStatusGrantsTaskAccess(TaskStatus taskStatus, Set<PwaAppProcessingPermission> permissions) {
    if (permissions.contains(SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY)) {
      return false;
    }
    return !(taskStatus.shouldForceInaccessible() || TaskStatus.COMPLETED.equals(taskStatus));
  }

  /**
   * Helper util to unify visible task status logic and logic for preventing access to task.
   */
  private TaskStatus getTaskStatus(PwaAppProcessingContext processingContext) {


    var appComplete = PwaApplicationStatus.COMPLETE.equals(processingContext.getApplicationDetail().getStatus());

    var optionsApprovalStatus = approveOptionsService.getOptionsApprovalStatus(
        processingContext.getApplicationDetail()
    );

    var openAppUpdate = applicationUpdateRequestService.applicationHasOpenUpdateRequest(
        processingContext.getApplicationDetail()
    );

    TaskStatus taskStatus;

    if (!optionsApprovalStatus.isConsentedOptionConfirmed() && appComplete) {
      taskStatus = TaskStatus.COMPLETED;

    } else if (!optionsApprovalStatus.isConsentedOptionConfirmed()) {

      if (openAppUpdate || !optionsApprovalStatus.isOptionsApproved()
          || OptionsApprovalStatus.APPROVED_UNRESPONDED.equals(optionsApprovalStatus)
      ) {
        taskStatus = TaskStatus.CANNOT_START_YET;
      } else {
        taskStatus = TaskStatus.NOT_STARTED;
      }
    } else {
      taskStatus = TaskStatus.NOT_REQUIRED;
    }

    return taskStatus;

  }


  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    TaskStatus taskStatus = getTaskStatus(processingContext);
    var isAccessible = taskStatusGrantsTaskAccess(taskStatus, processingContext.getAppProcessingPermissions());

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        TaskTag.from(taskStatus),
        isAccessible ? TaskState.EDIT : TaskState.LOCK,
        task.getDisplayOrder());

  }

}
