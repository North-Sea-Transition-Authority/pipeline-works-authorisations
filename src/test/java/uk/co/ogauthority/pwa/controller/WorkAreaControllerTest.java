package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaContext;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaContextService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaContextTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaResult;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

@WebMvcTest(WorkAreaController.class)
@Import(PwaMvcTestConfiguration.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  @MockBean
  private WorkAreaService workAreaService;

  @MockBean
  private WorkAreaContextService workAreaContextService;

  @MockBean
  private MetricsProvider metricsProvider;

  @Mock
  private Appender appender;

  @Captor
  private ArgumentCaptor<LoggingEvent> loggingEventCaptor;


  private Timer timer;

  private AuthenticatedUserAccount pwaManagerUser = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));

  private WorkAreaContext pwaManagerWorkAreaContext = WorkAreaContextTestUtil.createPwaManagerContext(pwaManagerUser);

  @BeforeEach
  void setup() {

    var emptyResultPageView = setupFakeWorkAreaResultPageView(0);
    when(workAreaService.getWorkAreaResult(any(), eq(WorkAreaTab.REGULATOR_REQUIRES_ATTENTION), anyInt()))
        .thenReturn(new WorkAreaResult(emptyResultPageView, null, null));
    when(workAreaService.getWorkAreaResult(any(), eq(WorkAreaTab.REGULATOR_WAITING_ON_OTHERS), anyInt()))
        .thenReturn(new WorkAreaResult(emptyResultPageView, null, null));

    when(workAreaContextService.createWorkAreaContext(pwaManagerUser))
        .thenReturn(pwaManagerWorkAreaContext);

    timer = TimerMetricTestUtils.setupTimerMetric(
        WorkAreaController.class, "pwa.workAreaTabTimer", appender);
    when(metricsProvider.getWorkAreaTabTimer()).thenReturn(timer);

  }

  @Test
  void renderWorkArea_noWorkAreaPriv() throws Exception {
    var unauthorisedUserAccount = new AuthenticatedUserAccount(
        new WebUserAccount(),
        EnumSet.noneOf(PwaUserPrivilege.class));

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .with(user(unauthorisedUserAccount)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderWorkArea_noDefaultTab() throws Exception {

    when(workAreaContextService.createWorkAreaContext(pwaManagerUser))
        .thenReturn(WorkAreaContextTestUtil.createContextWithZeroUserTabs(pwaManagerUser));

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .with(user(pwaManagerUser)))
        .andExpect(status().isForbidden());

  }

  @Test
  void renderWorkArea_defaultTab() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .with(user(pwaManagerUser)))
        .andExpect(status().isOk());

  }

  @Test
  void renderWorkAreaTab_whenUserDoesNotHaveWorkAreaPriv() throws Exception {
    var unauthorisedUserAccount = new AuthenticatedUserAccount(
        new WebUserAccount(),
        EnumSet.noneOf(PwaUserPrivilege.class));

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkAreaTab(null, null, null, Optional.empty())))
        .with(user(unauthorisedUserAccount)))
        .andExpect(status().isForbidden());

  }

  @Test
  void renderWorkAreaTab_whenNoPageParamProvided_defaultsApplied_forAttentionTab() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, null, Optional.empty())))
        .with(user(pwaManagerUser)))
        .andExpect(status().isOk());

    verify(workAreaService, times(1))
        .getWorkAreaResult(pwaManagerWorkAreaContext, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, 0);

    verifyNoInteractions(analyticsService);

  }

  @Test
  void renderWorkAreaTab_whenNoPageParamProvided_defaultsApplied_backgroundTab() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS, null, Optional.empty())))
            .with(user(pwaManagerUser)))
        .andExpect(status().isOk());

    verify(workAreaService, times(1))
        .getWorkAreaResult(pwaManagerWorkAreaContext, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS, 0);

    verify(analyticsService).sendAnalyticsEvent(any(), eq(AnalyticsEventCategory.BACKGROUND_WORKAREA_TAB),
        eq(Map.of("tab", WorkAreaTab.REGULATOR_WAITING_ON_OTHERS.getLabel())));

  }

  @Test
  void renderWorkAreaTab_whenPageParamProvided() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class)
        .renderWorkAreaTab(null, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, 100, Optional.empty())))
        .with(user(pwaManagerUser)))
        .andExpect(status().isOk());

    verify(workAreaService, times(1))
        .getWorkAreaResult(pwaManagerWorkAreaContext, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, 100);
  }

  @Test
  void renderWorkAreaTab_notAllowedToAccessTab() throws Exception {

    when(workAreaContextService.createWorkAreaContext(pwaManagerUser))
        .thenReturn(WorkAreaContextTestUtil.createContextWithZeroUserTabs(pwaManagerUser));

    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class)
        .renderWorkAreaTab(null, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, null, Optional.empty())))
        .with(user(pwaManagerUser)))
        .andExpect(status().isForbidden());
  }

  private PageView<PwaApplicationWorkAreaItem> setupFakeWorkAreaResultPageView(int page) {
    var fakePage = WorkAreaApplicationSearchTestUtil.setupFakeApplicationSearchResultPage(
        List.of(),
        PageRequest.of(page, 10)
    );

    return PageView.fromPage(
        fakePage,
        "workAreaUri",
        PwaApplicationWorkAreaItem::new
    );

  }


  @Test
  void getWorkAreaModelAndView_timerMetricStarted_timeRecordedAndLogged() {

    var controller = new WorkAreaController(workAreaService, workAreaContextService,
        Mockito.mock(SystemAreaAccessService.class), metricsProvider, analyticsService);

    controller.renderWorkArea(null, pwaManagerUser, null);

    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "work-area tab");
  }



}