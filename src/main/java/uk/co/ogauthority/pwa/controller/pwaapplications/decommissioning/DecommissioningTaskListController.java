package uk.co.ogauthority.pwa.controller.pwaapplications.decommissioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;

@Controller
@RequestMapping("/pwa-application/decom/{applicationId}/tasks")
public class DecommissioningTaskListController {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final TaskListService taskListService;

  @Autowired
  public DecommissioningTaskListController(PwaApplicationDetailService pwaApplicationDetailService,
                                           TaskListService taskListService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.taskListService = taskListService;
  }

  @GetMapping
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId, AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      if (detail.getPwaApplicationType() != PwaApplicationType.DECOMMISSIONING) {
        throw new PwaEntityNotFoundException("Application of wrong type:" + detail.getPwaApplicationType());
      }
      return taskListService.getTaskListModelAndView(detail);

    });

  }

}
