package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.creation.ApplicationTypeUtils;
import uk.co.ogauthority.pwa.model.enums.PwaResourceType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Controller
@RequestMapping("/pwa-application/initial/new")
public class StartInitialPwaController {


  /**
   * Render of start page for initial PWA application.
   */
  @GetMapping
  public ModelAndView renderStartPage(@ModelAttribute("form") PwaHolderForm startForm) {
    return new ModelAndView("pwaApplication/startPages/initial")
        .addObject("startUrl", ReverseRouter.route(on(StartInitialPwaController.class).startInitialPwa(null, null, startForm.getResourceType())))
        .addObject("formattedDuration", ApplicationTypeUtils.getFormattedDuration(PwaApplicationType.INITIAL))
        .addObject("formattedMedianLineDuration",
            ApplicationTypeUtils.getFormattedMedianLineDuration(PwaApplicationType.INITIAL));
  }

  /**
   * Create initial PWA application and redirect to first task.
   */
  @PostMapping
  public ModelAndView startInitialPwa(AuthenticatedUserAccount user,
                                      RedirectAttributes redirectAttributes,
                                      PwaResourceType resourceType) {
    return ReverseRouter.redirect(on(PwaHolderController.class).renderHolderScreen(null, resourceType, null));
  }

}
