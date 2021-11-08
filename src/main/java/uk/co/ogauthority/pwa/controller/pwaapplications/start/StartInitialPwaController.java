package uk.co.ogauthority.pwa.controller.pwaapplications.start;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.PwaHolderController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.util.ApplicationTypeUtils;

@Controller
@RequestMapping("/pwa-application/initial/new")
public class StartInitialPwaController {


  /**
   * Render of start page for initial PWA application.
   */
  @GetMapping
  public ModelAndView renderStartPage() {
    return new ModelAndView("pwaApplication/startPages/initial")
        .addObject("startUrl", ReverseRouter.route(on(StartInitialPwaController.class).startInitialPwa(null)))
        .addObject("formattedDuration", ApplicationTypeUtils.getFormattedDuration(PwaApplicationType.INITIAL))
        .addObject("formattedMedianLineDuration",
            ApplicationTypeUtils.getFormattedMedianLineDuration(PwaApplicationType.INITIAL));
  }

  /**
   * Create initial PWA application and redirect to first task.
   */
  @PostMapping
  public ModelAndView startInitialPwa(AuthenticatedUserAccount user) {
    return ReverseRouter.redirect(on(PwaHolderController.class).renderHolderScreen(null, null));
  }

}
