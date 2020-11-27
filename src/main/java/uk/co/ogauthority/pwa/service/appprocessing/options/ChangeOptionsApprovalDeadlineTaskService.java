package uk.co.ogauthority.pwa.service.appprocessing.options;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.appprocessing.options.ChangeOptionsApprovalDeadlineController;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;

@Service
public class ChangeOptionsApprovalDeadlineTaskService implements AppProcessingService {

  private final ApproveOptionsService approveOptionsService;

  @Autowired
  public ChangeOptionsApprovalDeadlineTaskService(ApproveOptionsService approveOptionsService) {
    this.approveOptionsService = approveOptionsService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext pwaAppProcessingContext) {
    return hasEditPermissions(pwaAppProcessingContext);
  }

  private boolean hasEditPermissions(PwaAppProcessingContext pwaAppProcessingContext) {
    return pwaAppProcessingContext.hasProcessingPermission(PwaAppProcessingPermission.CHANGE_OPTIONS_APPROVAL_DEADLINE);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {
    var taskAccessible = taskAccessible(processingContext);
    var changeDeadlineRoute = ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class).renderChangeDeadline(
        processingContext.getMasterPwaApplicationId(),
        processingContext.getApplicationType(),
        null,
        null,
        null
    ));

    var taskTag = !taskAccessible ? TaskTag.from(TaskStatus.CANNOT_START_YET) : null;
    var taskRoute = taskAccessible ? changeDeadlineRoute : null;

    return new TaskListEntry(
        task.getTaskName(),
        taskRoute,
        taskTag,
        task.getDisplayOrder());
  }

  public boolean taskAccessible(PwaAppProcessingContext pwaAppProcessingContext) {
    var optionsApproved = approveOptionsService.optionsApproved(pwaAppProcessingContext.getPwaApplication());
    return hasEditPermissions(pwaAppProcessingContext) && optionsApproved;

  }

}
