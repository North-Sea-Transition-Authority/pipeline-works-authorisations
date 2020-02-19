package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.FastTrackForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;

@Controller
@RequestMapping("/application")
public class PwaApplicationController {

  private static LocalDate startDate = LocalDate.now().plusMonths(4);

  @GetMapping("/1/tasks")
  public ModelAndView viewTaskList() {
    var availableTasks = new HashMap<>(Map.of(
        "Administrative details", ReverseRouter.route(on(PwaApplicationController.class).viewAdministrativeDetails(null)),
        "Project information", ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(null)),
        "Application contacts", "/",
        "Users, operators and owners", "/"
    ));
    availableTasks.compute("Fast track", (String key, String oldValue) ->
        startDate.isBefore(LocalDate.now().plusMonths(3))
            ? ReverseRouter.route(on(PwaApplicationController.class).viewFastTrackInformation(null))
            : null
    );
    return new ModelAndView("pwaApplication/temporary/taskList")
        .addObject("availableTasks", availableTasks);
  }

  @GetMapping("/1/admin-details")
  public ModelAndView viewAdministrativeDetails(@ModelAttribute("form") AdministrativeDetailsForm administrativeDetailsForm) {
    return new ModelAndView("pwaApplication/temporary/administrativeDetails")
        .addObject("holderCompanyName", "ROYAL DUTCH SHELL");
  }

  @PostMapping("/1/admin-details")
  public ModelAndView postAdminDetails() {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList());
  }

  @GetMapping("/1/project-information")
  public ModelAndView viewProjectInformation(@ModelAttribute("form") ProjectInformationForm projectInformationForm) {
    return new ModelAndView("pwaApplication/temporary/projectInformation");
  }

  @PostMapping("/1/project-information")
  public ModelAndView postProjectInformation(@ModelAttribute("form") ProjectInformationForm projectInformationForm) {
    try {
      startDate = LocalDate.of(
          projectInformationForm.getWorkStartYear(),
          projectInformationForm.getWorkStartMonth(),
          projectInformationForm.getWorkStartDay()
      );
    } catch (Exception exception) {
      startDate = LocalDate.now().plusMonths(4);
    }
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList());
  }

  @GetMapping("/1/fast-track")
  public ModelAndView viewFastTrackInformation(@ModelAttribute("form") FastTrackForm fastTrackForm) {
    return new ModelAndView("pwaApplication/temporary/fastTrack")
        .addObject("projectInformationUrl", ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(null)));
  }

  @PostMapping("/1/fast-track")
  public ModelAndView postFastTrackInformation(@ModelAttribute("form") FastTrackForm fastTrackForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList());
  }

}
