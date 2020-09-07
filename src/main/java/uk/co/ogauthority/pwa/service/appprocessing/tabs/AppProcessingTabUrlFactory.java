package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class AppProcessingTabUrlFactory {

  private final PwaApplicationDetail applicationDetail;

  public AppProcessingTabUrlFactory(PwaApplicationDetail applicationDetail) {
    this.applicationDetail = applicationDetail;
  }

  public String getTabUrl(String tabValue) {

    var tab = AppProcessingTab.resolveByValue(tabValue);

    return ReverseRouter.route(on(CaseManagementController.class).renderCaseManagement(
        applicationDetail.getMasterPwaApplicationId(),
        applicationDetail.getPwaApplicationType(),
        tab,
        null,
        null
    ));

  }

}
