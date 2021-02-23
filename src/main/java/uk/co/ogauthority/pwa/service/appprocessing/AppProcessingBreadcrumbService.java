package uk.co.ogauthority.pwa.service.appprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.controller.appprocessing.prepareconsent.AppConsentDocController;
import uk.co.ogauthority.pwa.controller.consultations.ConsultationController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;

@Service
public class AppProcessingBreadcrumbService {

  public void fromCaseManagement(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, caseManagement(pwaApplication), thisPage);
  }

  public void fromPrepareConsent(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {

    var crumbs = caseManagement(pwaApplication);

    crumbs.put(ReverseRouter.route(on(AppConsentDocController.class)
        .renderConsentDocEditor(pwaApplication.getId(), pwaApplication.getApplicationType(), null, null)),
        "Prepare consent");

    addAttrs(modelAndView, crumbs, thisPage);

  }

  public void fromConsultations(PwaApplication application, ModelAndView modelAndView, String thisPage) {

    var crumbs = caseManagement(application);

    crumbs.put(ReverseRouter.route(on(ConsultationController.class).renderConsultations(application.getId(),
        application.getApplicationType(), null, null)), PwaAppProcessingTask.CONSULTATIONS.getTaskName());

    addAttrs(modelAndView, crumbs, thisPage);

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
