package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.creation.ApplicationTypeUtils;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.util.converters.ResourceTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{resourceType}/new")
public class StartInitialPwaController {


  /**
   * Render of start page for initial PWA application.
   */
  @GetMapping
  public ModelAndView renderStartPage(@PathVariable @ApplicationTypeUrl PwaApplicationType applicationType,
                                      @PathVariable @ResourceTypeUrl PwaResourceType resourceType) {
    var guideText = resourceType == PwaResourceType.HYDROGEN ? "applications" : "fields";

    return new ModelAndView("pwaApplication/startPages/initial")
        .addObject("startUrl", ReverseRouter.route(on(StartInitialPwaController.class).startInitialPwa(null, resourceType)))
        .addObject("formattedDuration", ApplicationTypeUtils.getFormattedDuration(PwaApplicationType.INITIAL))
        .addObject("formattedMedianLineDuration",
            ApplicationTypeUtils.getFormattedMedianLineDuration(PwaApplicationType.INITIAL))
        .addObject("resourceTypeGuideText", guideText);
  }

  /**
   * Create initial PWA application and redirect to first task.
   */
  @PostMapping
  public ModelAndView startInitialPwa(AuthenticatedUserAccount user,
                                      @ResourceTypeUrl PwaResourceType resourceType) {
    return ReverseRouter.redirect(on(PwaHolderController.class).renderHolderScreen(null, resourceType, null));
  }

}
