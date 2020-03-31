package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;
import uk.co.ogauthority.pwa.util.ApplicationTypeUtils;

@Controller
@RequestMapping("/prototype/pwa-application/initial")
public class StartPrototypeInitialPwaController {

  private final PwaApplicationService pwaApplicationService;

  @Autowired
  public StartPrototypeInitialPwaController(PwaApplicationService pwaApplicationService) {
    this.pwaApplicationService = pwaApplicationService;
  }

  /**
   * Render of start page for initial PWA application.
   */
  @GetMapping("/new")
  public ModelAndView renderStartPage() {
    ModelAndView modelAndView = new ModelAndView("pwaApplication/startPages/initial")
        .addObject("formattedDuration",
            ApplicationTypeUtils.getFormattedDuration(PwaApplicationType.INITIAL))
        .addObject("startUrl",
            ReverseRouter.route(on(StartPrototypeInitialPwaController.class).startInitialPwa(null)));
    return modelAndView;
  }

  /**
   * Create initial PWA application and redirect to first task.
   */
  @PostMapping("/new")
  public ModelAndView startInitialPwa(AuthenticatedUserAccount user) {
    PwaApplication pwaApplication = pwaApplicationService.createInitialPwaApplication(user);
    return ReverseRouter.redirect(
        on(PrototypePwaHolderController.class).renderHolderScreen(pwaApplication.getId(), null, null));
  }

}
