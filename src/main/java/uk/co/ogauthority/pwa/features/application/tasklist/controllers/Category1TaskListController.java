package uk.co.ogauthority.pwa.features.application.tasklist.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Controller
@RequestMapping("/pwa-application/cat-1/{applicationId}/tasks")
@PwaApplicationTypeCheck(types = {PwaApplicationType.CAT_1_VARIATION})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class Category1TaskListController {

  private final TaskListService taskListService;
  private final TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator;

  @Autowired
  public Category1TaskListController(TaskListService taskListService,
                                     TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator) {
    this.taskListService = taskListService;
    this.taskListControllerModelAndViewCreator = taskListControllerModelAndViewCreator;
  }

  @GetMapping
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId,
                                   PwaApplicationContext applicationContext) {
    var taskGroups = taskListService.getTaskListGroups(applicationContext.getApplicationDetail());
    return taskListControllerModelAndViewCreator.getTaskListModelAndView(
        applicationContext.getApplicationDetail(),
        taskGroups
    );
  }

}
