package uk.co.ogauthority.pwa.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartPwaApplicationController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.temp.controller.StartPrototypePwaApplicationController;

@Controller
@RequestMapping
public class WorkAreaController {

  private final WorkAreaService workAreaService;

  @Autowired
  public WorkAreaController(WorkAreaService workAreaService) {
    this.workAreaService = workAreaService;
  }

  /**
   * Gets the assigned task list for the logged-in user.
   *
   * @return stub work area screen
   */
  @GetMapping("/work-area")
  public ModelAndView renderWorkArea(AuthenticatedUserAccount authenticatedUserAccount,
                                     @RequestParam(defaultValue = "OPEN") WorkAreaTab tab,
                                     @RequestParam(defaultValue = "0") Integer page) {
    return new ModelAndView("workArea")
        .addObject("prototypeApplicationUrl",
            ReverseRouter.route(on(StartPrototypePwaApplicationController.class).renderStartApplication(null)))
        .addObject("startPwaApplicationUrl",
            ReverseRouter.route(on(StartPwaApplicationController.class).renderStartApplication(null)))
        .addObject("workAreaPageView", workAreaService.getWorkAreaResultPage(authenticatedUserAccount, tab, page));
  }

}