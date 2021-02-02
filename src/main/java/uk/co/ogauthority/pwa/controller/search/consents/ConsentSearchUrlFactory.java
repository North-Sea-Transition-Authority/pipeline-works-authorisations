package uk.co.ogauthority.pwa.controller.search.consents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.mvc.ReverseRouter;


public class ConsentSearchUrlFactory {

  public String getPwaViewRoute(Integer pwaId) {
    return ReverseRouter.route(on(PwaViewController.class)
        .renderViewPwa(pwaId, null, null));
  }



}
