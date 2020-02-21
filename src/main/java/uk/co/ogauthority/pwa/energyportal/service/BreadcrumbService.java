package uk.co.ogauthority.pwa.energyportal.service;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.controller.PwaApplicationController;

@Service
public class BreadcrumbService {

  private static final String BREADCRUMB_MAP_ATTR = "breadcrumbMap";
  private static final String CURRENT_PAGE_ATTR = "currentPage";

  public void fromWorkArea(ModelAndView modelAndView, String currentPage) {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();

    breadcrumbs.put(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()), "Work area");

    modelAndView.addObject(BREADCRUMB_MAP_ATTR, breadcrumbs);
    modelAndView.addObject(CURRENT_PAGE_ATTR, currentPage);
  }

  public void fromTaskList(ModelAndView modelAndView, String currentPage) {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();

    breadcrumbs.put(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()), "Work area");
    breadcrumbs.put(ReverseRouter.route(on(PwaApplicationController.class).viewTaskList()), "Task list");

    modelAndView.addObject(BREADCRUMB_MAP_ATTR, breadcrumbs);
    modelAndView.addObject(CURRENT_PAGE_ATTR, currentPage);
  }

  public void fromCrossingAgreements(ModelAndView modelAndView, String currentPage) {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();

    breadcrumbs.put(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()), "Work area");
    breadcrumbs.put(ReverseRouter.route(on(PwaApplicationController.class).viewTaskList()), "Task list");
    breadcrumbs.put(ReverseRouter.route(on(PwaApplicationController.class).viewCrossings(null)), "Crossing agreements");

    modelAndView.addObject(BREADCRUMB_MAP_ATTR, breadcrumbs);
    modelAndView.addObject(CURRENT_PAGE_ATTR, currentPage);
  }
}
