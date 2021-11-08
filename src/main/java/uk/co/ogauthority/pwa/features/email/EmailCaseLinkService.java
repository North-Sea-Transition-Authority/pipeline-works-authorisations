package uk.co.ogauthority.pwa.features.email;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission.ReviewAndSubmitController;
import uk.co.ogauthority.pwa.controller.search.consents.PwaPipelineViewController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.search.consents.PwaPipelineViewTab;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@Service
public class EmailCaseLinkService {

  @Value("${pwa.url.base}")
  private String pwaUrlBase;

  @Value("${context-path}")
  private String contextPath;

  public String generateCaseManagementLink(PwaApplication application) {
    return pwaUrlBase + contextPath + CaseManagementUtils.routeCaseManagement(application);
  }

  public String generateReviewAndSubmitLink(PwaApplication application) {
    return pwaUrlBase + contextPath + ReverseRouter.route(on(ReviewAndSubmitController.class)
        .review(application.getApplicationType(), application.getId(), null, null));
  }

  public String generateAsBuiltNotificationSummaryLink(Integer pwaId, Integer pipelineId) {
    return pwaUrlBase + contextPath + ReverseRouter.route(on(PwaPipelineViewController.class)
        .renderViewPwaPipeline(pwaId, pipelineId, PwaPipelineViewTab.AS_BUILT_NOTIFICATION_HISTORY, null, null,
            null, null, null));
  }

  public String generateAsBuiltNotificationWorkareaLink() {
    return pwaUrlBase + contextPath + ReverseRouter.route(on(WorkAreaController.class)
        .renderWorkAreaTab(null, WorkAreaTab.AS_BUILT_NOTIFICATIONS, null));
  }

}