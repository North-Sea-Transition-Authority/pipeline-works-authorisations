package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.model.contacts.UooAgreement;
import uk.co.ogauthority.pwa.temp.model.contacts.UooCompanyView;
import uk.co.ogauthority.pwa.temp.model.contacts.UooRole;
import uk.co.ogauthority.pwa.temp.model.contacts.UooTreatyView;
import uk.co.ogauthority.pwa.temp.model.contacts.UooType;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;
import uk.co.ogauthority.pwa.temp.model.form.UserOwnerOperatorForm;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/application")
public class PwaApplicationController {

  @GetMapping("/1/tasks")
  public ModelAndView viewTaskList() {
    return new ModelAndView("pwaApplication/temporary/taskList")
        .addObject("availableTasks", Map.of(
            "Administrative details", ReverseRouter.route(on(PwaApplicationController.class).viewAdministrativeDetails(null)),
            "Project information", ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(null)),
            "Application contacts", "/",
            "Users, operators and owners", ReverseRouter.route(on(PwaApplicationController.class).viewUserOwnerOperatorContacts())
        ));
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

  @GetMapping("/1/uoo-contacts")
  public ModelAndView viewUserOwnerOperatorContacts() {
    return new ModelAndView("pwaApplication/temporary/uooContacts/contacts")
        .addObject("uooCompanyList", makeUooCompanyViews())
        .addObject("uooTreatyList", makeUooTreatyViews())
        .addObject("newUooUrl", ReverseRouter.route(on(PwaApplicationController.class).viewNewUooContact(null)))
        .addObject("taskListUrl", ReverseRouter.route(on(PwaApplicationController.class).viewTaskList()));
  }

  @GetMapping("/1/uoo-contacts/new")
  public ModelAndView viewNewUooContact(@ModelAttribute("form") UserOwnerOperatorForm userOwnerOperatorForm) {
    return new ModelAndView("pwaApplication/temporary/uooContacts/new")
        .addObject("uooTypes", Arrays.stream(UooType.values())
          .collect(StreamUtils.toLinkedHashMap(UooType::name, UooType::toString)))
        .addObject("uooRoles", Arrays.stream(UooRole.values())
            .collect(StreamUtils.toLinkedHashMap(UooRole::name, UooRole::toString)))
        .addObject("uooAgreements", Arrays.stream(UooAgreement.values())
            .collect(StreamUtils.toLinkedHashMap(UooAgreement::name, UooAgreement::toString)));
  }

  @PostMapping("/1/uoo-contacts/new")
  public ModelAndView postNewUooContact(@ModelAttribute("form") UserOwnerOperatorForm userOwnerOperatorForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewUserOwnerOperatorContacts());
  }


  private List<UooCompanyView> makeUooCompanyViews() {
    var uooA = new UooCompanyView(
        135432, "Royal Dutch Shell PLC", "Shell Centre\nBishop's, London\nSE1 7NA",
        Set.of("User"));

    var uooB = new UooCompanyView(
        365478, "BP PLC", "1 St James's Square\nSt. James's\nLondon SW1Y 4PD",
        Set.of("Operator"));

    var uooC = new UooCompanyView(
        83625, "Perenco", "8 Hanover Square\nMayfair\nLondon\nW1S 1HQ",
        Set.of("Owner"));

    var uooD = new UooCompanyView(
        114234, "ConocoPhillips", "925 N Eldridge Pkwy\nHouston\nTX 77079\nUnited States",
        Set.of("User", "Owner"));

    return List.of(uooA, uooB, uooC, uooD);
  }

  private List<UooTreatyView> makeUooTreatyViews() {
    var uooA = new UooTreatyView(UooAgreement.NCS_EXPORT_GAS.toString(), Set.of("User"));
    return List.of(uooA);
  }

}
