package uk.co.ogauthority.pwa.service.pwaapplications;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.DefaultNotificationBannerType;
import uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerBodyLine;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ParallelApplicationsWarning;
import uk.co.ogauthority.pwa.util.NotificationBannerUtils;

/**
 * Service which is responsible for adding notification banners in relation to an application to a page.
 */
@Service
public class PwaAppNotificationBannerService {

  private final PwaApplicationService pwaApplicationService;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public PwaAppNotificationBannerService(PwaApplicationService pwaApplicationService,
                                         PwaApplicationDetailService pwaApplicationDetailService) {
    this.pwaApplicationService = pwaApplicationService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  public void addParallelPwaApplicationsWarningBannerIfRequired(PwaApplication pwaApplication, ModelAndView modelAndView) {
    if (pwaApplication.getApplicationType().getParallelApplicationsWarning() == ParallelApplicationsWarning.SHOW_WARNING) {
      var parallelAppRefs = getParallelPwaApplicationReferences(pwaApplication);
      if (!parallelAppRefs.isEmpty()) {
        NotificationBannerUtils.infoBanner(
            DefaultNotificationBannerType.PARALLEL_APPS_WARNING.getTitle(),
            List.of(
                new NotificationBannerBodyLine(String.join(", ", parallelAppRefs), "govuk-!-font-weight-bold"),
                new NotificationBannerBodyLine(DefaultNotificationBannerType.PARALLEL_APPS_WARNING.getDefaultBodyText(), null)
            ),
            modelAndView);
      }
    }
  }

  private List<String> getParallelPwaApplicationReferences(PwaApplication pwaApplication) {
    var parallelApplications = getParallelApplicationsThatNeedWarning(pwaApplication);

    var latestApplicationDetailsBeingProcessed = getLatestApplicationDetailsBeingProcessed(parallelApplications);

    var applicationToLatestDetailMap = latestApplicationDetailsBeingProcessed.stream()
        .collect(Collectors.toMap(PwaApplicationDetail::getPwaApplication, Function.identity()));

    return applicationToLatestDetailMap.keySet().stream()
        .map(PwaApplication::getAppReference)
        .collect(Collectors.toList());
  }

  private List<PwaApplication> getParallelApplicationsThatNeedWarning(PwaApplication pwaApplication) {
    return pwaApplicationService.getAllApplicationsForMasterPwa(pwaApplication.getMasterPwa()).stream()
        .filter(pwaApp -> !pwaApp.getId().equals(pwaApplication.getId())
            && pwaApp.getApplicationType().getParallelApplicationsWarning() == ParallelApplicationsWarning.SHOW_WARNING)
        .collect(Collectors.toList());
  }

  private List<PwaApplicationDetail> getLatestApplicationDetailsBeingProcessed(List<PwaApplication> pwaApplications) {
    return pwaApplicationDetailService.getLatestDetailsForApplications(pwaApplications).stream()
        .filter(appDetail -> ApplicationState.SUBMITTED.getStatuses().contains(appDetail.getStatus()))
        .collect(Collectors.toList());
  }

}
