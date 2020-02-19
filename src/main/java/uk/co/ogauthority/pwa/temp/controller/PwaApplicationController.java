package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.util.LinkedHashMap;
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
import uk.co.ogauthority.pwa.util.DateUtil;

@Controller
@RequestMapping("/application")
public class PwaApplicationController {

  private static LocalDate startDate = LocalDate.now().plusMonths(4);

  @GetMapping("/1/tasks")
  public ModelAndView viewTaskList() {
    var availableTasks = new LinkedHashMap<String, String>() {
      {
        put("Administrative details", ReverseRouter.route(on(PwaApplicationController.class).viewAdministrativeDetails(null)));
        put("Project information", ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(null)));
        put("Application contacts", "/");
        put("Users, operators and owners", "/");
      }
    };
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
        .addObject("projectInformationUrl", ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(null)))
        .addObject("startDate", DateUtil.formatDate(startDate))
        .addObject("minNotFastTrackStartDate", DateUtil.formatDate(LocalDate.now().plusMonths(3)));
  }

  @PostMapping("/1/fast-track")
  public ModelAndView postFastTrackInformation(@ModelAttribute("form") FastTrackForm fastTrackForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList());
  }

}
