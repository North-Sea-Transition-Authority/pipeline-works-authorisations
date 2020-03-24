package uk.co.ogauthority.pwa.controller.pwaapplications.start;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PickExistingPwaController;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.ApplicationTypeUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationTypePathUrl}/new")
public class StartVariationController {

  @GetMapping
  public ModelAndView renderVariationTypeStartPage(@PathVariable("applicationTypePathUrl")
                                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType) {
    ModelAndView modelAndView;

    switch (pwaApplicationType) {
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
        throw new AccessDeniedException(String.format("Application type not supported %s", pwaApplicationType));
    }

    return modelAndView
        .addObject("htmlTitle", "Start new " + pwaApplicationType.getDisplayName())
        .addObject("pageHeading", "Start new " + pwaApplicationType.getDisplayName())
        .addObject("typeDisplay", pwaApplicationType.getDisplayName())
        .addObject("formattedDuration", ApplicationTypeUtils.getFormattedDuration(pwaApplicationType))
        .addObject("formattedImplicationDuration",
            ApplicationTypeUtils.getFormattedImplicationDuration(pwaApplicationType))
        .addObject("buttonUrl", ReverseRouter.route(on(this.getClass()).startVariation(pwaApplicationType)));
  }

  @PostMapping
  public ModelAndView startVariation(@PathVariable("applicationTypePathUrl")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType) {
    switch (pwaApplicationType) {
      case HUOO_VARIATION:
      case CAT_1_VARIATION:
      case CAT_2_VARIATION:
      case DEPOSIT_CONSENT:
      case OPTIONS_VARIATION:
      case DECOMMISSIONING:
        return ReverseRouter.redirect(on(PickExistingPwaController.class)
            .renderPickPwaToStartApplication(pwaApplicationType, null, null));
      default:
        throw new AccessDeniedException(String.format("Application type not supported %s", pwaApplicationType));
    }

  }
}
