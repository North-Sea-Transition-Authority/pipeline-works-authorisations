package uk.co.ogauthority.pwa.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartPwaApplicationController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.controller.StartPrototypePwaApplicationController;

@Controller
@RequestMapping
public class WorkAreaController {

  /**
   * Gets the assigned task list for the logged-in user.
   * @return stub work area screen
   */
  @GetMapping("/work-area")
  public ModelAndView renderWorkArea() {
    return new ModelAndView("workArea")
        .addObject("prototypeApplicationUrl",
            ReverseRouter.route(on(StartPrototypePwaApplicationController.class).renderStartApplication(null)))
      .addObject("startPwaApplicationUrl",
          ReverseRouter.route(on(StartPwaApplicationController.class).renderStartApplication(null)));
  }

}