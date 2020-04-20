package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.LocationDetailsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CrossingAgreementsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class ApplicationBreadcrumbService {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public ApplicationBreadcrumbService(
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  public void fromWorkArea(ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, workArea(), thisPage);
  }

  public void fromCrossings(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(pwaApplication.getApplicationType(), null, null)),
        "Crossings");
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromLocationDetails(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(LocationDetailsController.class)
            .renderLocationDetails(pwaApplication.getApplicationType(), null, null, null)),
        "Location details");
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromTaskList(PwaApplication application, ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, taskList(application), thisPage);
  }

  private Map<String, String> taskList(PwaApplication application) {
    var map = workArea();
    String route = pwaApplicationRedirectService.getTaskListRoute(application);
    map.put(route, "Task list");
    return map;
  }

  private Map<String, String> workArea() {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()), "Work area");
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }

}
