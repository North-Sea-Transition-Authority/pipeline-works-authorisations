package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission.ReviewAndSubmitController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;

/**
 * Constructs task list entries on demand.
 */
@Service
public class TaskListEntryFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskListEntryFactory.class);


  private final ApplicationTaskService applicationTaskService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public TaskListEntryFactory(
      ApplicationTaskService applicationTaskService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.applicationTaskService = applicationTaskService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  TaskListEntry createApplicationTaskListEntry(PwaApplicationDetail pwaApplicationDetail,
                                               GeneralPurposeApplicationTask applicationTask) {
    LOGGER.info(String.format("Creating Task List Entry: %s", applicationTask.getShortenedDisplayName()));
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

  TaskListEntry createReviewAndSubmitTask(PwaApplicationDetail detail) {
    return new TaskListEntry(
        "Review and submit application",
        ReverseRouter.route(on(ReviewAndSubmitController.class)
            .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)),
        false,
        999);
  }

}
