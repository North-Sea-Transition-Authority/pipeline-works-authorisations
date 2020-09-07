package uk.co.ogauthority.pwa.service.appprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;

@Service
public class AppProcessingBreadcrumbService {

  public void fromCaseManagement(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, caseManagement(pwaApplication), thisPage);
  }

  private Map<String, String> caseManagement(PwaApplication application) {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(application.getId(), application.getApplicationType(), AppProcessingTab.TASKS, null, null)),
        application.getAppReference());
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }

}
