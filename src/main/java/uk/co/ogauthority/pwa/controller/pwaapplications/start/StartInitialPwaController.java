package uk.co.ogauthority.pwa.controller.pwaapplications.start;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.PwaHolderController;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;

@Controller
@RequestMapping("/pwa-application/initial")
public class StartInitialPwaController {

  private final PwaApplicationService pwaApplicationService;

  @Autowired
  public StartInitialPwaController(PwaApplicationService pwaApplicationService) {
    this.pwaApplicationService = pwaApplicationService;
  }

  /**
   * Render of start page for initial PWA application.
   */
  @GetMapping("/new")
  public ModelAndView renderStartPage() {
    ModelAndView modelAndView = new ModelAndView("pwaApplication/startPages/initial");
    modelAndView.addObject("startUrl", ReverseRouter.route(on(StartInitialPwaController.class).startInitialPwa(null)));
    return modelAndView;
  }

  /**
   * Create initial PWA application and redirect to first task.
   */
  @PostMapping("/new")
  public ModelAndView startInitialPwa(AuthenticatedUserAccount user) {
    PwaApplication pwaApplication = pwaApplicationService.createInitialPwaApplication(user);
    return ReverseRouter.redirect(on(PwaHolderController.class).renderHolderScreen(pwaApplication.getId(), null, null));
  }

}
