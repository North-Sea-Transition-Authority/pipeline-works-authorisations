package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;

@Controller
@RequestMapping("/pwa-application/initial/{applicationId}/tasks")
@PwaApplicationTypeCheck(types = { PwaApplicationType.INITIAL })
public class InitialTaskListController {

  private final TaskListService taskListService;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public InitialTaskListController(TaskListService taskListService,
                                   PwaApplicationDetailService pwaApplicationDetailService) {
    this.taskListService = taskListService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  @GetMapping
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId, AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      var pwaApplication = detail.getPwaApplication();
      if (pwaApplication.getApplicationType() != PwaApplicationType.INITIAL) {
        throw new PwaEntityNotFoundException("Application of wrong type:" + pwaApplication.getApplicationType());
      }

      return taskListService.getTaskListModelAndView(pwaApplication);

    });

  }

}
