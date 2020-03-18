package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.fields.InitialFieldsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;

@Controller
@RequestMapping("/pwa-application/initial/{applicationId}")
public class InitialTaskListController {

  private final ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public InitialTaskListController(ApplicationBreadcrumbService breadcrumbService) {
    this.breadcrumbService = breadcrumbService;
  }

  private List<TaskListEntry> getPwaInformationTaskList(Integer applicationId) {
    return List.of(
        new TaskListEntry("Consent holder",
            ReverseRouter.route(on(PwaHolderController.class).renderHolderScreen(applicationId, null, null)),
        false),
        new TaskListEntry("Field information",
            ReverseRouter.route(on(InitialFieldsController.class).renderFields(applicationId, null, null)),
            false)
    );
  }

  private List<TaskListEntry> getApplicationTaskList(Integer applicationId) {
    return List.of(
        new TaskListEntry("Environmental and decommissioning",
            ReverseRouter.route(on(EnvironmentalDecomController.class)
                .renderAdminDetails(PwaApplicationType.INITIAL, applicationId, null, null)),
            false)
    );
  }

  @GetMapping("/tasks")
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId) {
    var modelAndView = new ModelAndView("pwaApplication/initial/initialTaskList")
        .addObject("informationTasks", getPwaInformationTaskList(applicationId))
        .addObject("applicationTasks", getApplicationTaskList(applicationId));
    breadcrumbService.fromWorkArea(modelAndView, "Task list");
    return modelAndView;
  }

}
