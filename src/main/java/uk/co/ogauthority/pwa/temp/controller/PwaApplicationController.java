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
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.model.contacts.UooAgreement;
import uk.co.ogauthority.pwa.temp.model.contacts.UooAgreementView;
import uk.co.ogauthority.pwa.temp.model.contacts.UooCompanyView;
import uk.co.ogauthority.pwa.temp.model.contacts.UooRole;
import uk.co.ogauthority.pwa.temp.model.contacts.UooType;
import uk.co.ogauthority.pwa.temp.model.entity.BlockCrossing;
import uk.co.ogauthority.pwa.temp.model.entity.TelecommunicationCableCrossing;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.CrossingAgreementsForm;
import uk.co.ogauthority.pwa.temp.model.form.LocationForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;
import uk.co.ogauthority.pwa.temp.model.form.PwaContactForm;
import uk.co.ogauthority.pwa.temp.model.form.UserOwnerOperatorForm;
import uk.co.ogauthority.pwa.temp.model.form.crossings.BlockCrossingForm;
import uk.co.ogauthority.pwa.temp.model.form.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.temp.model.locations.MedianLineSelection;
import uk.co.ogauthority.pwa.temp.model.pwacontacts.ContactRole;
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
            "Location details", ReverseRouter.route(on(PwaApplicationController.class).viewLocationDetails(null)),
            "Crossings", ReverseRouter.route(on(PwaApplicationController.class).viewCrossings(null)),
            "Users, operators and owners", ReverseRouter.route(on(PwaApplicationController.class).viewUserOwnerOperatorContacts()),
            "PWA contacts", ReverseRouter.route(on(PwaApplicationController.class).viewApplicationContacts())
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
    return new ModelAndView("pwaApplication/temporary/pwaContacts/contacts")
        .addObject("contacts", makeContacts())
        .addObject("taskListUrl", ReverseRouter.route(on(PwaApplicationController.class).viewTaskList()))
        .addObject("addContactUrl", ReverseRouter.route(on(PwaApplicationController.class).viewNewApplicationContact(null)));
  }

  @GetMapping("/1/application-contacts/new")
  public ModelAndView viewNewApplicationContact(@ModelAttribute("form") PwaContactForm pwaContactForm) {
    return new ModelAndView("pwaApplication/temporary/pwaContacts/new")
        .addObject("roles", Arrays.stream(ContactRole.values())
          .collect(StreamUtils.toLinkedHashMap(Enum::name, Enum::toString)));
  }

  @PostMapping("/1/application-contacts/new")
  public ModelAndView postAddApplicationContact(@ModelAttribute("form") PwaContactForm pwaContactForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewApplicationContacts());
  }

  @GetMapping("/1/project-information")
  public ModelAndView viewProjectInformation(@ModelAttribute("form") ProjectInformationForm projectInformationForm) {
    return new ModelAndView("pwaApplication/temporary/projectInformation");
  }

  @GetMapping("/1/location-details")
  public ModelAndView viewLocationDetails(@ModelAttribute("form") LocationForm locationForm) {
    return new ModelAndView("pwaApplication/temporary/locationDetails")
        .addObject("medianLineSelections", Arrays.stream(MedianLineSelection.values())
          .collect(StreamUtils.toLinkedHashMap(Enum::name, Enum::toString))
        ).addObject("holderCompanyName", "ROYAL DUTCH SHELL");
  }

  @PostMapping("/1/location-details")
  public ModelAndView postLocationDetails(@ModelAttribute("form") LocationForm locationForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList());
  }

  @GetMapping("/1/crossings")
  public ModelAndView viewCrossings(@ModelAttribute("form") CrossingAgreementsForm crossingAgreementsForm) {
    return new ModelAndView("pwaApplication/temporary/crossingAgreements/crossings")
        .addObject("addBlockCrossingUrl", ReverseRouter.route(on(PwaApplicationController.class).viewAddBlockCrossing(null)))
        .addObject("addTelecommuncationCableCrossingUrl",
            ReverseRouter.route(on(PwaApplicationController.class).viewAddTelecommunicationCableCrossing(null)))
        .addObject("addTelecommuncationCableCrossingUrl",
            ReverseRouter.route(on(PwaApplicationController.class).viewAddTelecommunicationCableCrossing(null)))
        .addObject("addPipelineCrossingUrl", ReverseRouter.route(on(PwaApplicationController.class).viewAddPipelineCrossing(null)))
        .addObject("blockCrossings", makeBlockCrossings())
        .addObject("telecommunicationCableCrossings", makeTelecommunicationCableCrossings())
        .addObject("pipelineCrossings", List.of());
  }

  @PostMapping("/1/crossings")
  public ModelAndView postCrossings(@ModelAttribute("form") CrossingAgreementsForm crossingAgreementsForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList());
  }

  @GetMapping("/1/crossings/block-crossing/new")
  public ModelAndView viewAddBlockCrossing(@ModelAttribute("form") BlockCrossingForm blockCrossingForm) {
    return new ModelAndView("pwaApplication/temporary/crossingAgreements/newBlockCrossing");
  }

  @PostMapping("/1/crossings/block-crossing/new")
  public ModelAndView postAddBlockCrossing(@ModelAttribute("form") BlockCrossingForm blockCrossingForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewCrossings(null));
  }

  @GetMapping("/1/crossings/telecommunication-cable-crossing/new")
  public ModelAndView viewAddTelecommunicationCableCrossing(
      @ModelAttribute("form") TelecommunicationCableCrossing telecommunicationCableCrossing) {

    return new ModelAndView("pwaApplication/temporary/crossingAgreements/newTelecommunicationCableCrossing");
  }

  @PostMapping("/1/crossings/telecommunication-cable-crossing/new")
  public ModelAndView postAddTelecommunicationCableCrossing(
      @ModelAttribute("form") TelecommunicationCableCrossing telecommunicationCableCrossing) {

    return ReverseRouter.redirect(on(PwaApplicationController.class).viewCrossings(null));
  }

  @GetMapping("/1/crossings/pipeline-crossing/new")
  public ModelAndView viewAddPipelineCrossing(@ModelAttribute("form") PipelineCrossingForm pipelineCrossingForm) {
    return new ModelAndView("pwaApplication/temporary/crossingAgreements/newPipelineCrossing");
  }

  @PostMapping("/1/crossings/pipeline-crossing/new")
  public ModelAndView postAddPipelineCrossing(@ModelAttribute("form") PipelineCrossingForm pipelineCrossingForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewCrossings(null));
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

  private List<BlockCrossing> makeBlockCrossings() {
    var crossingA = new BlockCrossing("2/1a", 4, "HESS LIMITED");
    return List.of(crossingA);
  }

  private List<TelecommunicationCableCrossing> makeTelecommunicationCableCrossings() {
    var crossingA = new TelecommunicationCableCrossing("XXXX to XXXX Submarine Communications Cable", "HESS LIMITED");
    return List.of(crossingA);
  }

  private List<TeamMemberView> makeContacts() {

    var contactA = new TeamMemberView(
        new Person(1, "John", "Smith", "john.smith@test.co.uk", "0800 368 9345"),
        "/", "/",
        Set.of(
            new TeamRoleView("Drafter", "Contractor", "Can draft applications", 1)
        ));

    var contactB = new TeamMemberView(
        new Person(2, "Jane", "Doe", "jane.doe@test.co.uk", "+44 3000 201 010"),
        "/", "/",
        Set.of(
            new TeamRoleView("Submitter", "Submitter", "Can submit applications", 1)
        ));

    return List.of(contactA, contactB);
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

  private List<UooAgreementView> makeUooTreatyViews() {
    var uooA = new UooAgreementView(UooAgreement.NCS_EXPORT_GAS.toString(), Set.of("User"));
    return List.of(uooA);
  }

}
