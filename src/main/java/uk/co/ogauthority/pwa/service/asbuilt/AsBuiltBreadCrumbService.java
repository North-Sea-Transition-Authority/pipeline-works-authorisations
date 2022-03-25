package uk.co.ogauthority.pwa.service.asbuilt;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.asbuilt.AsBuiltNotificationController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;

@Service
public class AsBuiltBreadCrumbService {

  public void fromDashboard(Integer asBuiltNotificationGroupId, String asBuiltNotificationGroupRef, ModelAndView modelAndView,
                            String thisPage) {
    addAttrs(modelAndView, dashboard(asBuiltNotificationGroupId, asBuiltNotificationGroupRef), thisPage);
  }

  public void fromWorkArea(ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, workArea(), thisPage);
  }

  private Map<String, String> workArea() {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(WorkAreaController.class).renderWorkAreaTab(null,
        WorkAreaTab.AS_BUILT_NOTIFICATIONS, null, Optional.empty())), "Work area");
    return breadcrumbs;
  }

  private Map<String, String> dashboard(Integer asBuiltNotificationGroupId, String asBuiltNotificationGroupRef) {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(AsBuiltNotificationController.class)
            .getAsBuiltNotificationDashboard(asBuiltNotificationGroupId, null)),
        asBuiltNotificationGroupRef + " as-built notifications");
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }

}