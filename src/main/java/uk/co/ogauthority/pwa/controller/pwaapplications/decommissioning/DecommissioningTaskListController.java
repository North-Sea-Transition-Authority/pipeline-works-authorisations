package uk.co.ogauthority.pwa.controller.pwaapplications.decommissioning;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;

@Controller
@RequestMapping("/pwa-application/decom/{applicationId}/tasks")
public class DecommissioningTaskListController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaApplicationService pwaApplicationService;

  @Autowired
  public DecommissioningTaskListController(
      ApplicationBreadcrumbService breadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PwaApplicationService pwaApplicationService) {
    this.breadcrumbService = breadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaApplicationService = pwaApplicationService;
  }

  private List<TaskListEntry> getPwaInformationTaskList(PwaApplication pwaApplication) {
    return List.of(
        new TaskListEntry("No tasks",
            pwaApplicationRedirectService.getTaskListRoute(pwaApplication), false)
    );
  }

  private List<TaskListEntry> getApplicationTaskList(PwaApplication pwaApplication) {
    return new ArrayList<>(List.of(
        new TaskListEntry("No tasks", pwaApplicationRedirectService.getTaskListRoute(pwaApplication), false)
    ));
  }

  @GetMapping
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId, AuthenticatedUserAccount user) {
    var application = pwaApplicationService.getApplicationFromId(applicationId);
    if (application.getApplicationType() != PwaApplicationType.DECOMMISSIONING) {
      throw new PwaEntityNotFoundException("Application of wrong type:" + application.getApplicationType());
    }

    // TODO: PWA-361 - Remove hard-coded "PWA-Example-BP-2".
    var modelAndView = new ModelAndView("pwaApplication/decommissioning/decommissioningTaskList")
        .addObject("informationTasks", getPwaInformationTaskList(application))
        .addObject("applicationTasks", getApplicationTaskList(application))
        .addObject("masterPwaReference", "PWA-Example-BP-2");
    breadcrumbService.fromWorkArea(modelAndView, "Task list");
    return modelAndView;
  }

}
