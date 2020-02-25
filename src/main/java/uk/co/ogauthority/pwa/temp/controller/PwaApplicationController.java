package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.temp.model.contacts.UooAgreement;
import uk.co.ogauthority.pwa.temp.model.contacts.UooAgreementView;
import uk.co.ogauthority.pwa.temp.model.contacts.UooCompanyView;
import uk.co.ogauthority.pwa.temp.model.contacts.UooRole;
import uk.co.ogauthority.pwa.temp.model.contacts.UooType;
import uk.co.ogauthority.pwa.temp.model.entity.BlockCrossing;
import uk.co.ogauthority.pwa.temp.model.entity.TelecommunicationCableCrossing;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.CrossingAgreementsForm;
import uk.co.ogauthority.pwa.temp.model.form.FastTrackForm;
import uk.co.ogauthority.pwa.temp.model.form.LocationForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;
import uk.co.ogauthority.pwa.temp.model.form.PwaContactForm;
import uk.co.ogauthority.pwa.temp.model.form.UserOwnerOperatorForm;
import uk.co.ogauthority.pwa.temp.model.form.crossings.BlockCrossingForm;
import uk.co.ogauthority.pwa.temp.model.form.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.temp.model.locations.MedianLineSelection;
import uk.co.ogauthority.pwa.temp.model.pwacontacts.ContactRole;
import uk.co.ogauthority.pwa.util.DateUtil;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/application/{applicationId}")
public class PwaApplicationController {

  private static LocalDate startDate = LocalDate.now().plusMonths(4);
  private LinkedHashMap<String, String> taskList;
  private final ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public PwaApplicationController(ApplicationBreadcrumbService breadcrumbService) {
    this.breadcrumbService = breadcrumbService;
  }

  private LinkedHashMap<String, String> getTaskList(Integer applicationId) {
    return new LinkedHashMap<>() {
      {
        put("PWA contacts",
            ReverseRouter.route(on(PwaApplicationController.class).viewApplicationContacts(applicationId)));
        put("Project information",
            ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(applicationId, null)));
        put("Users, operators and owners",
            ReverseRouter.route(on(PwaApplicationController.class).viewUserOwnerOperatorContacts(applicationId)));
        put("Administrative details",
            ReverseRouter.route(on(PwaApplicationController.class).viewAdministrativeDetails(applicationId, null)));
        put("Pipelines",
            ReverseRouter.route(on(PipelinesController.class).pipelines(applicationId)));
        put("Crossings",
            ReverseRouter.route(on(PwaApplicationController.class).viewCrossings(applicationId, null)));
        put("Location details",
            ReverseRouter.route(on(PwaApplicationController.class).viewLocationDetails(applicationId, null)));
      }
    };
  }


  @GetMapping("/tasks")
  public ModelAndView viewTaskList(@PathVariable("applicationId") Integer applicationId) {
    var taskList = getTaskList(applicationId);
    taskList.compute("Fast track", (String key, String oldValue) ->
        startDate.isBefore(LocalDate.now().plusMonths(3))
            ? ReverseRouter.route(on(PwaApplicationController.class).viewFastTrackInformation(applicationId, null))
            : null
    );
    var modelAndView = new ModelAndView("pwaApplication/temporary/taskList")
        .addObject("availableTasks", taskList);

    breadcrumbService.fromWorkArea(modelAndView, "Task list");
    return modelAndView;
  }

  @GetMapping("/admin-details")
  public ModelAndView viewAdministrativeDetails(@PathVariable("applicationId") Integer applicationId,
                                                @ModelAttribute("form") AdministrativeDetailsForm administrativeDetailsForm) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/administrativeDetails")
        .addObject("holderCompanyName", "ROYAL DUTCH SHELL");
    breadcrumbService.fromTaskList(modelAndView, "Administrative details");
    return modelAndView;
  }

  @PostMapping("/admin-details")
  public ModelAndView postAdminDetails(@PathVariable("applicationId") Integer applicationId) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList(applicationId));
  }

  @GetMapping("/application-contacts")
  public ModelAndView viewApplicationContacts(@PathVariable("applicationId") Integer applicationId) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/pwaContacts/contacts")
        .addObject("contacts", makeContacts())
        .addObject("taskListUrl", ReverseRouter.route(on(PwaApplicationController.class).viewTaskList(applicationId)))
        .addObject("addContactUrl", ReverseRouter.route(on(PwaApplicationController.class).viewNewApplicationContact(applicationId, null)));

    breadcrumbService.fromTaskList(modelAndView, "PWA contacts");
    return modelAndView;
  }

  @GetMapping("/application-contacts/new")
  public ModelAndView viewNewApplicationContact(@PathVariable("applicationId") Integer applicationId,
                                                @ModelAttribute("form") PwaContactForm pwaContactForm) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/pwaContacts/new")
        .addObject("roles", Arrays.stream(ContactRole.values())
            .collect(StreamUtils.toLinkedHashMap(Enum::name, Enum::toString)));

    breadcrumbService.fromPwaContacts(modelAndView, "Add contact");
    return modelAndView;
  }

  @PostMapping("/application-contacts/new")
  public ModelAndView postAddApplicationContact(@PathVariable("applicationId") Integer applicationId,
                                                @ModelAttribute("form") PwaContactForm pwaContactForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewApplicationContacts(applicationId));
  }

  @GetMapping("/project-information")
  public ModelAndView viewProjectInformation(@PathVariable("applicationId") Integer applicationId,
                                             @ModelAttribute("form") ProjectInformationForm projectInformationForm) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/projectInformation");
    breadcrumbService.fromTaskList(modelAndView, "Project information");
    return modelAndView;
  }

  @PostMapping("/project-information")
  public ModelAndView postProjectInformation(@PathVariable("applicationId") Integer applicationId,
                                             @ModelAttribute("form") ProjectInformationForm projectInformationForm) {
    try {
      startDate = LocalDate.of(
          projectInformationForm.getWorkStartYear(),
          projectInformationForm.getWorkStartMonth(),
          projectInformationForm.getWorkStartDay()
      );
    } catch (Exception exception) {
      startDate = LocalDate.now().plusMonths(4);
    }
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList(applicationId));
  }

  @GetMapping("/fast-track")
  public ModelAndView viewFastTrackInformation(@PathVariable("applicationId") Integer applicationId,
                                               @ModelAttribute("form") FastTrackForm fastTrackForm) {
    return new ModelAndView("pwaApplication/temporary/fastTrack")
        .addObject("projectInformationUrl",
            ReverseRouter.route(on(PwaApplicationController.class).viewProjectInformation(applicationId, null)))
        .addObject("startDate", DateUtil.formatDate(startDate))
        .addObject("minNotFastTrackStartDate", DateUtil.formatDate(LocalDate.now().plusMonths(3)));
  }

  @PostMapping("/fast-track")
  public ModelAndView postFastTrackInformation(@PathVariable("applicationId") Integer applicationId,
                                               @ModelAttribute("form") FastTrackForm fastTrackForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList(applicationId));
  }

  @GetMapping("/location-details")
  public ModelAndView viewLocationDetails(@PathVariable("applicationId") Integer applicationId,
                                          @ModelAttribute("form") LocationForm locationForm) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/locationDetails")
        .addObject("medianLineSelections", Arrays.stream(MedianLineSelection.values())
            .collect(StreamUtils.toLinkedHashMap(Enum::name, Enum::toString))
        ).addObject("holderCompanyName", "ROYAL DUTCH SHELL");

    breadcrumbService.fromTaskList(modelAndView, "Location details");
    return modelAndView;
  }

  @PostMapping("/location-details")
  public ModelAndView postLocationDetails(@PathVariable("applicationId") Integer applicationId,
                                          @ModelAttribute("form") LocationForm locationForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList(applicationId));
  }

  @GetMapping("/crossings")
  public ModelAndView viewCrossings(@PathVariable("applicationId") Integer applicationId,
                                    @ModelAttribute("form") CrossingAgreementsForm crossingAgreementsForm) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/crossingAgreements/crossings")
        .addObject("addBlockCrossingUrl", ReverseRouter.route(on(PwaApplicationController.class)
            .viewAddBlockCrossing(applicationId, null)))
        .addObject("addTelecommuncationCableCrossingUrl", ReverseRouter.route(on(PwaApplicationController.class)
                .viewAddTelecommunicationCableCrossing(applicationId, null))
        )
        .addObject("addTelecommuncationCableCrossingUrl", ReverseRouter.route(on(PwaApplicationController.class)
            .viewAddTelecommunicationCableCrossing(applicationId, null))
        )
        .addObject("addPipelineCrossingUrl", ReverseRouter.route(on(PwaApplicationController.class)
            .viewAddPipelineCrossing(applicationId, null))
        )
        .addObject("blockCrossings", makeBlockCrossings())
        .addObject("telecommunicationCableCrossings", makeTelecommunicationCableCrossings())
        .addObject("pipelineCrossings", List.of());

    breadcrumbService.fromTaskList(modelAndView, "Crossing agreements");
    return modelAndView;
  }

  @PostMapping("/crossings")
  public ModelAndView postCrossings(@PathVariable("applicationId") Integer applicationId,
                                    @ModelAttribute("form") CrossingAgreementsForm crossingAgreementsForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList(applicationId));
  }

  @GetMapping("/crossings/block-crossing/new")
  public ModelAndView viewAddBlockCrossing(@PathVariable("applicationId") Integer applicationId,
                                           @ModelAttribute("form") BlockCrossingForm blockCrossingForm) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/crossingAgreements/newBlockCrossing");

    breadcrumbService.fromCrossingAgreements(modelAndView, "Block crossing");
    return modelAndView;
  }

  @PostMapping("/crossings/block-crossing/new")
  public ModelAndView postAddBlockCrossing(@PathVariable("applicationId") Integer applicationId,
                                           @ModelAttribute("form") BlockCrossingForm blockCrossingForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewCrossings(applicationId, null));
  }

  @GetMapping("/crossings/telecommunication-cable-crossing/new")
  public ModelAndView viewAddTelecommunicationCableCrossing(
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") TelecommunicationCableCrossing telecommunicationCableCrossing) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/crossingAgreements/newTelecommunicationCableCrossing");

    breadcrumbService.fromCrossingAgreements(modelAndView, "Telecommunication cable crossing");
    return modelAndView;
  }

  @PostMapping("/crossings/telecommunication-cable-crossing/new")
  public ModelAndView postAddTelecommunicationCableCrossing(
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") TelecommunicationCableCrossing telecommunicationCableCrossing) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewCrossings(applicationId, null));
  }

  @GetMapping("/crossings/pipeline-crossing/new")
  public ModelAndView viewAddPipelineCrossing(@PathVariable("applicationId") Integer applicationId,
                                              @ModelAttribute("form") PipelineCrossingForm pipelineCrossingForm) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/crossingAgreements/newPipelineCrossing");

    breadcrumbService.fromCrossingAgreements(modelAndView, "Pipeline crossing");
    return modelAndView;
  }

  @PostMapping("/crossings/pipeline-crossing/new")
  public ModelAndView postAddPipelineCrossing(@PathVariable("applicationId") Integer applicationId,
                                              @ModelAttribute("form") PipelineCrossingForm pipelineCrossingForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewCrossings(applicationId, null));
  }

  @GetMapping("/uoo-contacts")
  public ModelAndView viewUserOwnerOperatorContacts(@PathVariable("applicationId") Integer applicationId) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/uooContacts/contacts")
        .addObject("uooCompanyList", makeUooCompanyViews())
        .addObject("uooTreatyList", makeUooTreatyViews())
        .addObject("newUooUrl", ReverseRouter.route(on(PwaApplicationController.class).viewNewUooContact(applicationId, null)))
        .addObject("taskListUrl", ReverseRouter.route(on(PwaApplicationController.class).viewTaskList(applicationId)));

    breadcrumbService.fromTaskList(modelAndView, "Users, operator, owners");
    return modelAndView;
  }

  @GetMapping("/uoo-contacts/new")
  public ModelAndView viewNewUooContact(@PathVariable("applicationId") Integer applicationId,
                                        @ModelAttribute("form") UserOwnerOperatorForm userOwnerOperatorForm) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/uooContacts/new")
        .addObject("uooTypes", Arrays.stream(UooType.values())
            .collect(StreamUtils.toLinkedHashMap(UooType::name, UooType::toString)))
        .addObject("uooRoles", Arrays.stream(UooRole.values())
            .collect(StreamUtils.toLinkedHashMap(UooRole::name, UooRole::toString)))
        .addObject("uooAgreements", Arrays.stream(UooAgreement.values())
            .collect(StreamUtils.toLinkedHashMap(UooAgreement::name, UooAgreement::toString)));

    breadcrumbService.fromUoo(modelAndView, "Add user, operator or owner");
    return modelAndView;
  }

  @PostMapping("/uoo-contacts/new")
  public ModelAndView postNewUooContact(@PathVariable("applicationId") Integer applicationId,
                                        @ModelAttribute("form") UserOwnerOperatorForm userOwnerOperatorForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewUserOwnerOperatorContacts(applicationId));
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
