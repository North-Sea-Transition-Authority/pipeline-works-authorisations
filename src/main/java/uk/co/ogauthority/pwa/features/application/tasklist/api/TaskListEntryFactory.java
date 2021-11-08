package uk.co.ogauthority.pwa.features.application.tasklist.api;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.submission.controller.ReviewAndSubmitController;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;

/**
 * Constructs task list entries on demand.
 */
@Service
public class TaskListEntryFactory {

  private final ApplicationTaskService applicationTaskService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public TaskListEntryFactory(ApplicationTaskService applicationTaskService,
                              PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.applicationTaskService = applicationTaskService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  TaskListEntry createApplicationTaskListEntry(PwaApplicationDetail pwaApplicationDetail,
                                               GeneralPurposeApplicationTask applicationTask) {
    return new TaskListEntry(
        applicationTask.getDisplayName(),
        applicationTask.getTaskLandingPageRoute(pwaApplicationDetail.getPwaApplication()),
        applicationTaskService.isTaskComplete(applicationTask, pwaApplicationDetail),
        applicationTaskService.getTaskInfoList(applicationTask, pwaApplicationDetail),
        applicationTask.getDisplayOrder()
    );
  }

  TaskListEntry createNoTasksEntry(PwaApplication pwaApplication) {
    return new TaskListEntry(
        "No tasks",
        pwaApplicationRedirectService.getTaskListRoute(pwaApplication),
        false,
        0);
  }

  public TaskListEntry createReviewAndSubmitTask(PwaApplicationDetail detail) {
    return new TaskListEntry(
        "Review and submit application",
        ReverseRouter.route(on(ReviewAndSubmitController.class)
            .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)),
        false,
        999);
  }

}
