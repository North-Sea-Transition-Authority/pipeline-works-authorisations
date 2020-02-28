package uk.co.ogauthority.pwa.controller.pwaapplications.category1;

import java.util.LinkedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;

@Controller
@RequestMapping("/pwa-application/cat-1/{applicationId}/tasks")
public class Category1TaskListController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaApplicationService pwaApplicationService;

  @Autowired
  public Category1TaskListController(
      ApplicationBreadcrumbService breadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PwaApplicationService pwaApplicationService) {
    this.breadcrumbService = breadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaApplicationService = pwaApplicationService;
  }

  private LinkedHashMap<String, String> getPwaInformationTaskList(PwaApplication pwaApplication) {
    return new LinkedHashMap<>() {
      {
        put("No tasks", pwaApplicationRedirectService.getTaskListRoute(pwaApplication));

      }
    };
  }

  private LinkedHashMap<String, String> getApplicationTaskList(PwaApplication pwaApplication) {
    return new LinkedHashMap<>() {
      {
        put("No tasks", pwaApplicationRedirectService.getTaskListRoute(pwaApplication));
      }
    };
  }

  @GetMapping
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId) {
    var application = pwaApplicationService.getApplicationFromId(applicationId);
    if (application.getApplicationType() != PwaApplicationType.CAT_1_VARIATION) {
      throw new PwaEntityNotFoundException("Application of wrong type:" + application.getApplicationType());
    }

    var modelAndView = new ModelAndView("pwaApplication/category1/cat1TaskList")
        .addObject("pwaInformationTasks", getPwaInformationTaskList(application))
        .addObject("applicationTasks", getApplicationTaskList(application));
    breadcrumbService.fromWorkArea(modelAndView, "Task list");
    return modelAndView;
  }


}
