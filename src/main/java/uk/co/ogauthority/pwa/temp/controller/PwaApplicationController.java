package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;

@Controller
@RequestMapping("/application")
public class PwaApplicationController {

  @GetMapping("/1/tasks")
  public ModelAndView viewTaskList() {
    return new ModelAndView("pwaApplication/temporary/taskList")
        .addObject("availableTasks", Map.of(
            "Administrative details", ReverseRouter.route(on(PwaApplicationController.class).viewAdministrativeDetails(null)),
            "Project information", ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(null)),
            "Application contacts", ReverseRouter.route(on(PwaApplicationController.class).viewApplicationContacts()),
            "Users, operators and owners", "/"
        ));
  }

  @GetMapping("/1/admin-details")
  public ModelAndView viewAdministrativeDetails(@ModelAttribute("form") AdministrativeDetailsForm administrativeDetailsForm) {
    return new ModelAndView("pwaApplication/temporary/administrativeDetails")
        .addObject("holderCompanyName", "ROYAL DUTCH SHELL");
  }

  @PostMapping("/1/admin-details")
  public ModelAndView postAdministrativeDetails() {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList());
  }

  @GetMapping("/1/application-contacts")
  public ModelAndView viewApplicationContacts() {
    return new ModelAndView("pwaApplication/temporary/applicationContacts")
        .addObject("contacts", makeContacts())
        .addObject("linkToTaskList", ReverseRouter.route(on(PwaApplicationController.class).viewAdministrativeDetails(null)));
  }

  @GetMapping("/1/project-information")
  public ModelAndView viewProjectInformation(@ModelAttribute("form") ProjectInformationForm projectInformationForm) {
    return new ModelAndView("pwaApplication/temporary/projectInformation");
  }

  private List<TeamMemberView> makeContacts() {

    var contactA = new TeamMemberView(
        new Person(1, "John", "Smith", "john.smith@test.co.uk"),
        "/", "/",
        Set.of(
            new TeamRoleView("Drafter", "Drafter", "Can draft applications", 1)
        ));

    var contactB = new TeamMemberView(
        new Person(2, "Jane", "Doe", "jane.doe@test.co.uk"),
        "/", "/",
        Set.of(
            new TeamRoleView("Drafter", "Drafter", "Can draft applications", 1),
            new TeamRoleView("Submitter", "Submitter", "Can submit applications", 2)
        ));

    return List.of(contactA, contactB);
  }

}
