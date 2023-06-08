package uk.co.ogauthority.pwa.controller.search.consents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;

public class ConsentSearchUrlFactory {

  private final String searchUrl = ReverseRouter.route(on(ConsentSearchController.class)
      .renderSearch(null, null));

  public String getPwaViewRoute(Integer pwaId) {
    return ReverseRouter.route(on(PwaViewController.class)
        .renderViewPwa(pwaId, PwaViewTab.PIPELINES, null, null, null));
  }

  public String getSearchUrl() {
    return searchUrl;
  }

}
