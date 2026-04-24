package uk.co.ogauthority.pwa.features.application.tasklist.controllers;

import org.springframework.context.annotation.Profile;
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

@Profile(uk.co.ogauthority.pwa.config.Profile.ENABLE_PRUAT_ENHANCEMENTS)
@Controller
@RequestMapping("/pwa-application/pipeline-record-management/{applicationId}/tasks")
@PwaApplicationTypeCheck(types = {PwaApplicationType.PIPELINE_RECORD_MANAGEMENT})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class PipelineRecordManagementTaskListController {

  private final TaskListService taskListService;
  private final TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator;

  PipelineRecordManagementTaskListController(
      TaskListService taskListService,
      TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator
  ) {
    this.taskListService = taskListService;
    this.taskListControllerModelAndViewCreator = taskListControllerModelAndViewCreator;
  }

  @GetMapping
  public ModelAndView viewTaskList(
      @PathVariable Integer applicationId,
      PwaApplicationContext applicationContext
  ) {
    var taskGroups = taskListService.getTaskListGroups(applicationContext.getApplicationDetail());
    return taskListControllerModelAndViewCreator.getTaskListModelAndView(applicationContext.getApplicationDetail(), taskGroups);
  }

}
