package uk.co.ogauthority.pwa.controller.pwaapplications.deposit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.tasklist.TaskListControllerModelAndViewCreator;

@Controller
@RequestMapping("/pwa-application/dep/{applicationId}/tasks")
@PwaApplicationTypeCheck(types = {PwaApplicationType.DEPOSIT_CONSENT})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class DepositConsentTaskListController {

  private final TaskListService taskListService;
  private final TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator;

  @Autowired
  public DepositConsentTaskListController(TaskListService taskListService,
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
