package uk.co.ogauthority.pwa.util;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

/**
 * Utility class to provide common routing methods.
 */
public class RouteUtils {

  private RouteUtils() {
    throw new AssertionError();
  }

  public static ModelAndView redirectWorkArea() {
    return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));
  }

  public static String routeWorkArea() {
    return ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null));
  }

}
