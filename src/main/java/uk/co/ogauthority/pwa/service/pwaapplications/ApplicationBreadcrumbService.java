package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.controller.PrototypePwaApplicationController;

@Service
public class ApplicationBreadcrumbService {

  public void fromWorkArea(ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, workArea(), thisPage);
  }

  public void fromTaskList(Integer applicationId, ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, taskList(applicationId), thisPage);
  }

  private Map<String, String> workArea() {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()), "Work area");
    return breadcrumbs;
  }

  private Map<String, String> taskList(Integer applicationId) {
    Map<String, String> breadcrumbs = workArea();
    breadcrumbs.put(ReverseRouter.route(on(PrototypePwaApplicationController.class).viewTaskList(applicationId)), "Task list");
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }
}
