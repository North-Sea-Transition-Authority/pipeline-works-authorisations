package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.controller.search.consents.PwaPipelineViewController;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.search.consents.PwaPipelineViewTab;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;

public class PwaViewUrlFactory {

  private final int pwaId;

  public PwaViewUrlFactory(int pwaId) {
    this.pwaId = pwaId;
  }

  public String getTabUrl(String tabValue) {
    var tab = PwaViewTab.resolveByValue(tabValue);
    return ReverseRouter.route(on(PwaViewController.class)
        .renderViewPwa(pwaId, tab, null, null));
  }

  public String getConsentDocumentUrl(Integer pwaConsentId, Long docgenRunId) {
    return ReverseRouter.route(on(PwaViewController.class).downloadConsentDocument(pwaId, null, pwaConsentId, docgenRunId));
  }

  public String routeCaseManagement(Integer pwaApplicationId, PwaApplicationType applicationType) {

    return ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(pwaApplicationId, applicationType, AppProcessingTab.TASKS, null, null));
  }

  public String getPwaPipelineViewUrl(Integer pipelineId) {
    return ReverseRouter.route(on(PwaPipelineViewController.class)
        .renderViewPwaPipeline(pwaId, pipelineId, PwaPipelineViewTab.PIPELINE_HISTORY, null, null, null, null, null));
  }

}
