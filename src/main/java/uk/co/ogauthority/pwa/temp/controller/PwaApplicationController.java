package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.PwaApplicationScope;
import uk.co.ogauthority.pwa.temp.model.admindetails.WithinSafetyZone;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@Scope("request")
@RequestMapping("/application")
public class PwaApplicationController {

  private final PwaApplicationScope pwaApplicationScope;

  @Autowired
  public PwaApplicationController(PwaApplicationScope pwaApplicationScope) {
    this.pwaApplicationScope = pwaApplicationScope;
  }

  @GetMapping("/1/tasks")
  public ModelAndView viewTaskList() {
    return new ModelAndView("pwaApplication/temporary/taskList")
        .addObject("availableTasks", Map.of(
            "Administrative details", ReverseRouter.route(on(PwaApplicationController.class).viewAdministrativeDetails(null)),
            "Project information", ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(null)),
            "Application contacts", "/",
            "Users, operators and owners", "/"
        ));
  }

  @GetMapping("/1/admin-details")
  public ModelAndView viewAdministrativeDetails(@ModelAttribute("form") AdministrativeDetailsForm administrativeDetailsForm) {
    pwaApplicationScope.apply(administrativeDetailsForm);
    return new ModelAndView("pwaApplication/temporary/administrativeDetails")
        .addObject("holderCompanyName", "ROYAL DUTCH SHELL")
        .addObject("withinSafetyZone", Arrays.stream(WithinSafetyZone.values())
          .collect(StreamUtils.toLinkedHashMap(Enum::name, Enum::toString)));
  }

  @PostMapping("/1/admin-details")
  public ModelAndView postAdminDetails(@ModelAttribute("form") AdministrativeDetailsForm administrativeDetailsForm) {
    pwaApplicationScope.save(administrativeDetailsForm);
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList());
  }

  @GetMapping("/1/project-information")
  public ModelAndView viewProjectInformation(@ModelAttribute("form") ProjectInformationForm projectInformationForm) {
    pwaApplicationScope.apply(projectInformationForm);
    return new ModelAndView("pwaApplication/temporary/projectInformation");
  }

  @PostMapping("/1/project-information")
  public ModelAndView postProjectInformation(@ModelAttribute("form") ProjectInformationForm projectInformationForm) {
    pwaApplicationScope.save(projectInformationForm);
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList());
  }

}
