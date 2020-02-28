package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;

@Controller
@RequestMapping("/pwa-application/initial/{applicationId}")
public class InitialTaskList {

  private final ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public InitialTaskList(ApplicationBreadcrumbService breadcrumbService) {
    this.breadcrumbService = breadcrumbService;
  }

  private LinkedHashMap<String, String> getPwaInformationTaskList(Integer applicationId) {
    return new LinkedHashMap<>() {
      {
        put("Consent holder", ReverseRouter.route(on(PwaHolderController.class).renderHolderScreen(applicationId, null, null))
        );
      }
    };
  }

  private LinkedHashMap<String, String> getApplicationTaskList(Integer applicationId) {
    return new LinkedHashMap<>() {
      {
        put("No tasks", ReverseRouter.route(on(InitialTaskList.class).viewTaskList(applicationId)));
      }
    };
  }

  @GetMapping("/tasks")
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId) {
    var modelAndView = new ModelAndView("pwaApplication/initial/taskList")
        .addObject("pwaInformationTasks", getPwaInformationTaskList(applicationId))
        .addObject("applicationTasks", getApplicationTaskList(applicationId));
    breadcrumbService.fromWorkArea(modelAndView, "Task list");
    return modelAndView;
  }

}
