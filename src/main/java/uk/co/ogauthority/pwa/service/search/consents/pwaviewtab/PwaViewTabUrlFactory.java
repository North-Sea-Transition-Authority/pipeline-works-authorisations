package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;

public class PwaViewTabUrlFactory {

  private final int pwaId;

  public PwaViewTabUrlFactory(int pwaId) {
    this.pwaId = pwaId;
  }

  public String getTabUrl(String tabValue) {

    var tab = PwaViewTab.resolveByValue(tabValue);

    return ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
        pwaId,
        tab,
        null,
        null
    ));

  }

}
