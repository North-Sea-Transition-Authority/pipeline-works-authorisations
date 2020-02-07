package uk.co.ogauthority.pwa.controller.pwaapplications.start;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;

@Controller
@RequestMapping("/pwa-application/initial")
public class StartInitialPwaController {

  private final PwaApplicationService pwaApplicationService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public StartInitialPwaController(PwaApplicationService pwaApplicationService,
                                   PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.pwaApplicationService = pwaApplicationService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  @GetMapping("/new")
  public ModelAndView renderStartPage() {
    ModelAndView modelAndView = new ModelAndView("pwaApplication/startPages/initial");
    modelAndView.addObject("startUrl", ReverseRouter.route(on(StartInitialPwaController.class).startInitialPwa(null)));
    return modelAndView;
  }

  @PostMapping("/new")
  public ModelAndView startInitialPwa(AuthenticatedUserAccount user) {
    pwaApplicationService.createInitialPwaApplication(user);
    return pwaApplicationRedirectService.getTaskListRedirect(PwaApplicationType.INITIAL);
  }

}
