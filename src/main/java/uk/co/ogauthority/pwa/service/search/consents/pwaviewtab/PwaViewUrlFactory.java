package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.search.consents.PwaPipelineViewController;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.casemanagement.AppProcessingTab;
import uk.co.ogauthority.pwa.features.appprocessing.casemanagement.controller.CaseManagementController;
import uk.co.ogauthority.pwa.features.consents.viewconsent.controller.ConsentFileController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.search.consents.PwaPipelineViewTab;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;

public class PwaViewUrlFactory {

  private final int pwaId;

  public PwaViewUrlFactory(int pwaId) {
    this.pwaId = pwaId;
  }

  public String getTabUrl(String tabValue, Boolean showBreadcrumbs) {
    var tab = PwaViewTab.resolveByValue(tabValue);
    return ReverseRouter.route(on(PwaViewController.class)
        .renderViewPwa(pwaId, tab, null, null, showBreadcrumbs));
  }

  public String getConsentDocumentUrl(Integer pwaConsentId, Long docgenRunId) {
    return ReverseRouter.route(on(ConsentFileController.class).downloadConsentDocument(pwaId, pwaConsentId, null));
  }

  public String getConsentDocumentsUrl(Integer pwaConsentId) {
    return ReverseRouter.route(on(ConsentFileController.class).renderViewConsentDocuments(pwaId, pwaConsentId, null, null));
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
