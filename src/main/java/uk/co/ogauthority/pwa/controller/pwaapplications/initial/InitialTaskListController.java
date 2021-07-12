package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.tasklist.TaskListControllerModelAndViewCreator;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;

@Controller
@RequestMapping("/pwa-application/initial/{applicationId}/tasks")
@PwaApplicationTypeCheck(types = {PwaApplicationType.INITIAL})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class InitialTaskListController {

  private final TaskListService taskListService;
  private final TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator;
  private final MetricsProvider metricsProvider;

  private static final Logger LOGGER = LoggerFactory.getLogger(InitialTaskListController.class);

  @Autowired
  public InitialTaskListController(TaskListService taskListService,
                                   TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator,
                                   MetricsProvider metricsProvider) {
    this.taskListService = taskListService;
    this.taskListControllerModelAndViewCreator = taskListControllerModelAndViewCreator;
    this.metricsProvider = metricsProvider;
  }

  @GetMapping
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId,
                                   PwaApplicationContext applicationContext) {

    var stopwatch = Stopwatch.createStarted();

    var taskGroups = taskListService.getTaskListGroups(applicationContext.getApplicationDetail());
    var modelAndView =  taskListControllerModelAndViewCreator.getTaskListModelAndView(
        applicationContext.getApplicationDetail(),
        taskGroups
    );

    MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getTaskListTimer(), "Task list loaded.");

    return modelAndView;
  }

}
