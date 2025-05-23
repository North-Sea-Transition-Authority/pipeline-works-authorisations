package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.DefaultNotificationBannerType;
import uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerBodyLine;
import uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ParallelApplicationsWarning;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaAppNotificationBannerServiceTest {

  @Mock
  private PwaApplicationService pwaApplicationService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private PwaAppNotificationBannerService pwaAppNotificationBannerService;

  private final PwaApplicationDetail pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION,
      10, 20);

  private final PwaApplication siblingPwaApplication = new PwaApplication(pwaApplicationDetail.getMasterPwa(), PwaApplicationType.CAT_1_VARIATION, 10);

  private PwaApplicationDetail siblingPwaApplicationDetail;

  @BeforeEach
  void setup() {
    pwaAppNotificationBannerService = new PwaAppNotificationBannerService(pwaApplicationService, pwaApplicationDetailService);

    siblingPwaApplication.setId(11);
    siblingPwaApplication.setAppReference("PWA_REF_2");

    siblingPwaApplicationDetail = PwaApplicationTestUtil.createApplicationDetail(siblingPwaApplication.getMasterPwa(),
        PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM, PwaApplicationStatus.CASE_OFFICER_REVIEW, siblingPwaApplication.getId(), 22, 10);
    siblingPwaApplicationDetail.setPwaApplication(siblingPwaApplication);

    when(pwaApplicationService.getAllApplicationsForMasterPwa(pwaApplicationDetail.getMasterPwa()))
        .thenReturn(List.of(pwaApplicationDetail.getPwaApplication(), siblingPwaApplicationDetail.getPwaApplication()));
  }

  @Test
  void addParallelPwaApplicationsWarningBannerIfRequired() {
    when(pwaApplicationDetailService.getLatestDetailsForApplications(List.of(siblingPwaApplicationDetail.getPwaApplication())))
        .thenReturn(List.of(siblingPwaApplicationDetail));

    var modelAndView = new ModelAndView();
    var bannerView = new NotificationBannerView.BannerBuilder(DefaultNotificationBannerType.PARALLEL_APPS_WARNING.getTitle())
        .addBodyLine(new NotificationBannerBodyLine(siblingPwaApplication.getAppReference(), "govuk-!-font-weight-bold"))
        .addBodyLine(new NotificationBannerBodyLine(DefaultNotificationBannerType.PARALLEL_APPS_WARNING.getDefaultBodyText(), null))
        .build();
    pwaAppNotificationBannerService.addParallelPwaApplicationsWarningBannerIfRequired(pwaApplicationDetail.getPwaApplication(),
        modelAndView);
    assertThat(modelAndView.getModel()).containsKey("notificationBannerView");
    var modelBannerView = (NotificationBannerView) modelAndView.getModel().get("notificationBannerView");
    assertThat(modelBannerView.getTitle()).isEqualTo(bannerView.getTitle());
    assertThat(modelBannerView.getBodyLines()).isEqualTo(bannerView.getBodyLines());

  }

  @Test
  void addParallelPwaApplicationsWarningBannerIfRequired_appTypeDoesNotRequireParallelWarning() {
    siblingPwaApplication.setApplicationType(PwaApplicationType.INITIAL);

    var modelAndView = new ModelAndView();

    pwaAppNotificationBannerService
        .addParallelPwaApplicationsWarningBannerIfRequired(pwaApplicationDetail.getPwaApplication(), modelAndView);
    assertThat(modelAndView.getModel()).doesNotContainKey("notificationBannerView");
  }

  @Test
  void addParallelPwaApplicationsWarningBannerIfRequired_shownForAllAppTypesThatRequireParallelWarning() {
    when(pwaApplicationDetailService.getLatestDetailsForApplications(List.of(siblingPwaApplicationDetail.getPwaApplication())))
        .thenReturn(List.of(siblingPwaApplicationDetail));

    var appsRequiringWarning = PwaApplicationType.stream()
        .filter(appType -> appType.getParallelApplicationsWarning().equals(ParallelApplicationsWarning.SHOW_WARNING))
        .toList();

    appsRequiringWarning.forEach(appType -> {
      siblingPwaApplication.setApplicationType(appType);
      var modelAndView = new ModelAndView();

      pwaAppNotificationBannerService
          .addParallelPwaApplicationsWarningBannerIfRequired(pwaApplicationDetail.getPwaApplication(), modelAndView);
      assertThat(modelAndView.getModel()).containsKey("notificationBannerView");
    });

  }

  @Test
  void addParallelPwaApplicationsWarningBannerIfRequired_parallelApplicationExists_parallelAppIsNotBeingProcessed() {
    when(pwaApplicationDetailService.getLatestDetailsForApplications(List.of(siblingPwaApplicationDetail.getPwaApplication())))
        .thenReturn(List.of(siblingPwaApplicationDetail));
    siblingPwaApplicationDetail.setStatus(PwaApplicationStatus.COMPLETE);

    var modelAndView = new ModelAndView();

    pwaAppNotificationBannerService.addParallelPwaApplicationsWarningBannerIfRequired(pwaApplicationDetail.getPwaApplication(),
        modelAndView);
    assertThat(modelAndView.getModel()).doesNotContainKey("notificationBannerView");
  }

}
