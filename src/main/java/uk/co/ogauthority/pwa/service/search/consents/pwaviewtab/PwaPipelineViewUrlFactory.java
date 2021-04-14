package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.search.consents.PwaPipelineViewController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.search.consents.PwaPipelineViewTab;

public class PwaPipelineViewUrlFactory {

  private final int pwaId;
  private final int pipelineId;

  public PwaPipelineViewUrlFactory(int pwaId, int pipelineId) {
    this.pwaId = pwaId;
    this.pipelineId = pipelineId;
  }

  public String getTabUrl(String tabValue) {
    var tab = PwaPipelineViewTab.resolveByValue(tabValue);
    return ReverseRouter.route(on(PwaPipelineViewController.class)
        .renderViewPwaPipeline(pwaId, pipelineId, tab, null, null, null, null));
  }

}
