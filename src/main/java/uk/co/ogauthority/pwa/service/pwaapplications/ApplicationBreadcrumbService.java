package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.controller.PwaApplicationController;

@Service
public class ApplicationBreadcrumbService {

  public void fromWorkArea(ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, workArea(), thisPage);
  }

  public void fromTaskList(ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, taskList(), thisPage);
  }

  public void fromCrossingAgreements(ModelAndView modelAndView, String thisPage) {
    Map<String, String> breadcrumbs = taskList();
    breadcrumbs.put(ReverseRouter.route(on(PwaApplicationController.class).viewCrossings(null)), "Crossing agreements");

    addAttrs(modelAndView, breadcrumbs, thisPage);
  }

  public void fromPwaContacts(ModelAndView modelAndView, String thisPage) {
    Map<String, String> breadcrumbs = taskList();
    breadcrumbs.put(ReverseRouter.route(on(PwaApplicationController.class).viewApplicationContacts()), "PWA contacts");

    addAttrs(modelAndView, breadcrumbs, thisPage);
  }

  public void fromUoo(ModelAndView modelAndView, String thisPage) {
    Map<String, String> breadcrumbs = taskList();
    breadcrumbs.put(ReverseRouter.route(on(PwaApplicationController.class).viewUserOwnerOperatorContacts()), "Users, operator, owners");

    addAttrs(modelAndView, breadcrumbs, thisPage);
  }

  private Map<String, String> workArea() {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()), "Work area");
    return breadcrumbs;
  }

  private Map<String, String> taskList() {
    Map<String, String> breadcrumbs = workArea();
    breadcrumbs.put(ReverseRouter.route(on(PwaApplicationController.class).viewTaskList()), "Task list");
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }
}
