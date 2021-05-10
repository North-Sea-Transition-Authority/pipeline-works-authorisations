package uk.co.ogauthority.pwa.controller.appprocessing.prepareconsent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ParallelConsentView;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

public class SendForApprovalUrlFactory {

  private Map<Integer, String> applicationAppSummaryUrls;

  public SendForApprovalUrlFactory(List<ParallelConsentView> parallelConsentViewList) {
    this.applicationAppSummaryUrls = parallelConsentViewList
        .stream()
        .collect(Collectors.toMap(
            ParallelConsentView::getPwaApplicationId,
            o -> CaseManagementUtils.routeApplicationSummary(o.getPwaApplicationId(), o.getPwaApplicationType())
        ));
  }

  public String getAppSummaryUrlByAppId(int appId) {
    return this.applicationAppSummaryUrls.get(appId);
  }
}
