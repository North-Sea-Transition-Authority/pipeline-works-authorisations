package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailViewTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView;
import uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryViewService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

public abstract class PwaAppProcessingContextAbstractControllerTest extends AbstractControllerTest {

  @Autowired
  protected PwaAppProcessingContextService appProcessingContextService;

  @Autowired
  protected PwaAppProcessingPermissionService processingPermissionService;

  @MockBean
  protected PwaApplicationContextService pwaApplicationContextService;

  @SpyBean
  protected ApplicationBreadcrumbService breadcrumbService;

  @MockBean
  private CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @SpyBean
  private AppProcessingBreadcrumbService appProcessingBreadcrumbService;

  @MockBean
  protected PwaContextService pwaContextService;

  @MockBean
  protected AppFileService appFileService;

  @SpyBean
  protected UserTypeService userTypeService;

  @MockBean
  protected ConsultationRequestService consultationRequestService;

  @MockBean
  protected CaseSummaryViewService caseSummaryViewService;

  @MockBean
  protected MetricsProvider metricsProvider;

  @MockBean
  private Appender appender;

  private Timer timer;

  @BeforeEach
  public void processingContextAbstractControllerTestSetup() {

    var appDetailView = ApplicationDetailViewTestUtil.createGenericDetailView();
    var caseSummaryView = CaseSummaryView.from(appDetailView, CaseSummaryViewService.CASE_SUMMARY_HEADER_ID);
    when(caseSummaryViewService.getCaseSummaryViewForAppDetail(any())).thenReturn(Optional.of(caseSummaryView));

    timer = TimerMetricTestUtils.setupTimerMetric(
        PwaAppProcessingContextService.class, "pwa.appContextTimer", appender);
    when(metricsProvider.getAppContextTimer()).thenReturn(timer);

  }

}