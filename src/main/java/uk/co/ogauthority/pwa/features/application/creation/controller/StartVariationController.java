package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.creation.ApplicationTypeUtils;
import uk.co.ogauthority.pwa.features.application.creation.MedianLineImplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.util.converters.ResourceTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{resourceType}/new/variation")
public class StartVariationController {

  @GetMapping
  public ModelAndView renderVariationTypeStartPage(@PathVariable @ApplicationTypeUrl PwaApplicationType applicationType,
                                                   @PathVariable @ResourceTypeUrl PwaResourceType resourceType) {
    ModelAndView modelAndView;

    switch (applicationType) {
      case CAT_1_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/category1");
        break;
      case CAT_2_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/category2");
        break;
      case HUOO_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/huooVariation");
        break;
      case DEPOSIT_CONSENT:
        modelAndView = new ModelAndView("pwaApplication/startPages/depositConsent");
        break;
      case OPTIONS_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/optionsVariation");
        break;
      case DECOMMISSIONING:
        modelAndView = new ModelAndView("pwaApplication/startPages/decommissioningVariation");
        break;
      default:
        throw new AccessDeniedException(String.format("Application type not supported %s", applicationType));
    }

    if (applicationType.getMedianLineImplication().equals(MedianLineImplication.TRUE)) {
      modelAndView.addObject("formattedMedianLineDuration",
          ApplicationTypeUtils.getFormattedMedianLineDuration(applicationType));
    }

    return modelAndView
        .addObject("htmlTitle", "Start new " + applicationType.getDisplayName())
        .addObject("pageHeading", "Start new " + applicationType.getDisplayName())
        .addObject("typeDisplay", applicationType.getDisplayName())
        .addObject("formattedDuration", ApplicationTypeUtils.getFormattedDuration(applicationType))
        .addObject("buttonUrl", ReverseRouter.route(on(this.getClass()).startVariation(applicationType, resourceType)));
  }

  @PostMapping
  public ModelAndView startVariation(@PathVariable @ApplicationTypeUrl PwaApplicationType applicationType,
                                     @PathVariable @ResourceTypeUrl PwaResourceType resourceType) {
    switch (applicationType) {
      case CAT_1_VARIATION:
        return ReverseRouter.redirect(on(PickExistingPwaController.class)
            .renderPickPwaToStartApplication(applicationType, resourceType, null, null));
      case HUOO_VARIATION:
      case CAT_2_VARIATION:
      case DEPOSIT_CONSENT:
      case OPTIONS_VARIATION:
      case DECOMMISSIONING:
        return ReverseRouter.redirect(on(PickExistingPwaController.class)
            .renderPickPwaToStartApplication(applicationType, PwaResourceType.PETROLEUM, null, null));
      default:
        throw new AccessDeniedException(String.format("Application type not supported %s", applicationType));
    }

  }
}
