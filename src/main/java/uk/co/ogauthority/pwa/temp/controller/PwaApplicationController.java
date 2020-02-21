package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.model.entity.BlockCrossing;
import uk.co.ogauthority.pwa.temp.model.entity.TelecommunicationCableCrossing;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.CrossingAgreementsForm;
import uk.co.ogauthority.pwa.temp.model.form.LocationForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;
import uk.co.ogauthority.pwa.temp.model.form.crossings.BlockCrossingForm;
import uk.co.ogauthority.pwa.temp.model.form.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.temp.model.locations.MedianLineSelection;
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
            "Users, operators and owners", "/",
            "Location details", ReverseRouter.route(on(PwaApplicationController.class).viewLocationDetails(null)),
            "Crossings", ReverseRouter.route(on(PwaApplicationController.class).viewCrossings(null))
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

}
