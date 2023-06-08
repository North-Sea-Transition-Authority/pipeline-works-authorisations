package uk.co.ogauthority.pwa.service.search.consents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.search.consents.ConsentSearchController;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class SearchPwaBreadcrumbService {


  public void fromPwaPipelineTab(Integer pwaId, String pwaRef, ModelAndView modelAndView, String thisPage) {

    var crumbs = getConsentSearchBreadcrumbs();
    crumbs.put(ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
        pwaId, PwaViewTab.PIPELINES, null, null, null)), pwaRef);

    addAttrs(modelAndView, crumbs, thisPage);
  }

  public void fromPwaView(ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, getConsentSearchBreadcrumbs(), thisPage);
  }

  public void fromPwaConsentTab(Integer pwaId, String pwaRef, ModelAndView modelAndView, String thisPage) {

    var crumbs = getConsentSearchBreadcrumbs();
    crumbs.put(ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
        pwaId, PwaViewTab.CONSENT_HISTORY, null, null, null)), pwaRef);

    addAttrs(modelAndView, crumbs, thisPage);

  }

  private Map<String, String> getConsentSearchBreadcrumbs() {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(ConsentSearchController.class)
            .renderSearch(null, null)), "Search PWAs");
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }

}
