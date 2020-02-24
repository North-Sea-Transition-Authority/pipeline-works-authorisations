package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
import uk.co.ogauthority.pwa.temp.model.entity.BlockCrossing;
import uk.co.ogauthority.pwa.temp.model.entity.TelecommunicationCableCrossing;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.CrossingAgreementsForm;
import uk.co.ogauthority.pwa.temp.model.form.FastTrackForm;
import uk.co.ogauthority.pwa.temp.model.form.LocationForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;
import uk.co.ogauthority.pwa.temp.model.form.PwaContactForm;
import uk.co.ogauthority.pwa.temp.model.form.crossings.BlockCrossingForm;
import uk.co.ogauthority.pwa.temp.model.form.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.temp.model.locations.MedianLineSelection;
import uk.co.ogauthority.pwa.temp.model.pwacontacts.ContactRole;
import uk.co.ogauthority.pwa.util.DateUtil;
import uk.co.ogauthority.pwa.util.StreamUtils;

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
        put("Users, operators and owners", "/");
        put("PWA contacts", ReverseRouter.route(on(PwaApplicationController.class).viewApplicationContacts()));
        put("Crossings", ReverseRouter.route(on(PwaApplicationController.class).viewCrossings(null)));
        put("Location details", ReverseRouter.route(on(PwaApplicationController.class).viewLocationDetails(null)));
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

}
