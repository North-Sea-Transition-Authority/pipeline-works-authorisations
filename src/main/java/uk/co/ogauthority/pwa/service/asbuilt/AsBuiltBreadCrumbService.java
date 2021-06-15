package uk.co.ogauthority.pwa.service.asbuilt;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.asbuilt.AsBuiltNotificationController;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class AsBuiltBreadCrumbService {

  public void fromDashboard(AsBuiltNotificationGroup asBuiltNotificationGroup, ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, dashboard(asBuiltNotificationGroup), thisPage);
  }

  private Map<String, String> dashboard(AsBuiltNotificationGroup asBuiltNotificationGroup) {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(AsBuiltNotificationController.class)
            .getAsBuiltNotificationDashboard(asBuiltNotificationGroup.getId(), null)),
        asBuiltNotificationGroup.getReference() + " as-built notifications");
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }
}