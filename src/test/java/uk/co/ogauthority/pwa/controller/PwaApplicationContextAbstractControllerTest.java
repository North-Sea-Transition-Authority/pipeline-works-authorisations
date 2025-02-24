package uk.co.ogauthority.pwa.controller;

import static org.mockito.Mockito.when;

import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermissionService;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

public abstract class PwaApplicationContextAbstractControllerTest extends AbstractControllerTest {

  @Autowired
  protected PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  protected PadPipelineService padPipelineService;

  @SpyBean
  private CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @MockBean
  protected PadFileService padFileService;

  @MockBean
  protected DevukFieldService devukFieldService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

  @MockBean
  protected PwaApplicationPermissionService pwaApplicationPermissionService;

  @MockBean
  protected PwaHolderTeamService pwaHolderTeamService;

  @MockBean
  protected PwaContextService pwaContextService;

  @MockBean
  protected MetricsProvider metricsProvider;

  @MockBean
  private Appender appender;

  private Timer timer;

  @BeforeEach
  public void applicationContextAbstractControllerTestSetup() {

    timer = TimerMetricTestUtils.setupTimerMetric(
        PwaAppProcessingContextService.class, "pwa.appContextTimer", appender);
    when(metricsProvider.getAppContextTimer()).thenReturn(timer);

  }

}