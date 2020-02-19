package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;

@Controller
@RequestMapping("/application/{applicationId}")
public class PwaApplicationController {

  @GetMapping("/tasks")
  public ModelAndView viewTaskList(@PathVariable Integer applicationId) {
    return new ModelAndView("pwaApplication/temporary/taskList")
        .addObject("availableTasks", Map.of(
            "Administrative details", ReverseRouter.route(on(PwaApplicationController.class)
                .viewAdministrativeDetails(applicationId, null)),
            "Project information", ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(applicationId, null)),
            "Application contacts", "/",
            "Users, operators and owners", "/",
            "Pipelines", ReverseRouter.route(on(PipelinesController.class).pipelines(applicationId))
        ));
  }

  @GetMapping("/admin-details")
  public ModelAndView viewAdministrativeDetails(@PathVariable Integer applicationId,
                                                @ModelAttribute("form") AdministrativeDetailsForm administrativeDetailsForm) {
    return new ModelAndView("pwaApplication/temporary/administrativeDetails")
        .addObject("holderCompanyName", "ROYAL DUTCH SHELL");
  }

  @PostMapping("/admin-details")
  public ModelAndView postAdminDetails(@PathVariable Integer applicationId) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList(applicationId));
  }

  @GetMapping("/project-information")
  public ModelAndView viewProjectInformation(@PathVariable Integer applicationId,
                                             @ModelAttribute("form") ProjectInformationForm projectInformationForm) {
    return new ModelAndView("pwaApplication/temporary/projectInformation");
  }

}
