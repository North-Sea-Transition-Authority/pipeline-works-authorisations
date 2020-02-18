package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.model.entity.BlockCrossing;
import uk.co.ogauthority.pwa.temp.model.entity.PipelineCrossing;
import uk.co.ogauthority.pwa.temp.model.entity.TelecommunicationCrossing;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.CrossingAgreementsForm;
import uk.co.ogauthority.pwa.temp.model.form.CrossingForm;
import uk.co.ogauthority.pwa.temp.model.form.LocationForm;
import uk.co.ogauthority.pwa.temp.model.locations.CrossingType;
import uk.co.ogauthority.pwa.temp.model.locations.MedianLineSelection;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/application")
public class PwaApplicationController {

  @GetMapping("/1/admin-details")
  public ModelAndView viewAdministrativeDetails(@ModelAttribute("form") AdministrativeDetailsForm administrativeDetailsForm) {
    return new ModelAndView("pwaApplication/temporary/administrativeDetails")
        .addObject("holderCompanyName", "ROYAL DUTCH SHELL");
  }

  @GetMapping("/1/locations")
  public ModelAndView viewLocationDetails(@ModelAttribute("form") LocationForm locationForm) {
    return new ModelAndView("pwaApplication/temporary/locationDetails")
        .addObject("medianLineSelections", Arrays.stream(MedianLineSelection.values())
          .collect(StreamUtils.toLinkedHashMap(Enum::name, Enum::toString))
        ).addObject("holderCompanyName", "ROYAL DUTCH SHELL");
  }

  @GetMapping("/1/crossings")
  public ModelAndView viewCrossings(@ModelAttribute("form") CrossingAgreementsForm crossingAgreementsForm) {
    return new ModelAndView("pwaApplication/temporary/crossingAgreements/crossings")
        .addObject("addCrossingLink", ReverseRouter.route(on(PwaApplicationController.class).viewAddCrossing(null)))
        .addObject("blockCrossings", makeBlockCrossings())
        .addObject("telecommunicationCrossings", makeTelecommunicationCrossings())
        .addObject("pipelineCrossings", new PipelineCrossing[]{});
  }

  @GetMapping("/1/crossings/new")
  public ModelAndView viewAddCrossing(@ModelAttribute("form") CrossingForm crossingForm) {
    return new ModelAndView("pwaApplication/temporary/crossingAgreements/addCrossing")
        .addObject("crossingTypes", Arrays.stream(CrossingType.values())
            .collect(StreamUtils.toLinkedHashMap(Enum::name, Enum::toString))
        );
  }

  @PostMapping("/1/crossings/new")
  public ModelAndView postAddCrossing(@ModelAttribute("form") CrossingForm crossingForm) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewCrossings(null));
  }

  private BlockCrossing[] makeBlockCrossings() {
    var crossingA = new BlockCrossing("2/1a", 4, "HESS LIMITED");
    return new BlockCrossing[]{crossingA};
  }

  private TelecommunicationCrossing[] makeTelecommunicationCrossings() {
    var crossingA = new TelecommunicationCrossing("XXXX to XXXX Submarine Communications Cable", "HESS LIMITED");
    return new TelecommunicationCrossing[]{crossingA};
  }

}
