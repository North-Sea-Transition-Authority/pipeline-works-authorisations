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
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationTypePathUrl}/new")
public class StartVariationController {

  @GetMapping
  public ModelAndView renderVariationTypeStartPage(@PathVariable("applicationTypePathUrl")
                                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType) {
    ModelAndView modelAndView;

    switch (pwaApplicationType) {
      // TODO PWA-299, PWA-300, PWA-301, PWA-302 add case clause
      case CAT_1_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/category1");
        break;
      case CAT_2_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/category2");
        break;
      default:
        throw new AccessDeniedException(String.format("Application type not supported %s", pwaApplicationType));
    }

    return modelAndView
        .addObject("htmlTitle", "Start new " + pwaApplicationType.getDisplayName())
        .addObject("pageHeading", "Start new " + pwaApplicationType.getDisplayName())
        .addObject("buttonUrl", ReverseRouter.route(on(this.getClass()).startVariation(pwaApplicationType)));
  }

  @PostMapping
  public ModelAndView startVariation(@PathVariable("applicationTypePathUrl")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType) {
    switch (pwaApplicationType) {
      // TODO PWA-299, PWA-300, PWA-301, PWA-302 add case clause and fall through to single return statement
      case CAT_1_VARIATION:
      case CAT_2_VARIATION:
        return ReverseRouter.redirect(on(PickExistingPwaController.class)
            .renderPickPwaToStartApplication(pwaApplicationType, null, null));
      default:
        throw new AccessDeniedException(String.format("Application type not supported %s", pwaApplicationType));
    }

  }
}
