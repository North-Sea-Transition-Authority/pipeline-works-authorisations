package uk.co.ogauthority.pwa.controller.pwaapplications.huoo;

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
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;

@Controller
@RequestMapping("/pwa-application/huoo/{applicationId}/tasks")
@PwaApplicationTypeCheck(types = {PwaApplicationType.HUOO_VARIATION})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class HuooVariationTaskListController {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final TaskListService taskListService;

  @Autowired
  public HuooVariationTaskListController(PwaApplicationDetailService pwaApplicationDetailService,
                                         TaskListService taskListService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.taskListService = taskListService;
  }

  @GetMapping
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId,
                                   PwaApplicationContext applicationContext) {

    return taskListService.getTaskListModelAndView(applicationContext.getApplicationDetail());


  }

}
