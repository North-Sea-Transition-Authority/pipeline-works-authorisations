package uk.co.ogauthority.pwa.features.application.tasklist.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTaskService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListEntryFactory;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaAppNotificationBannerService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationPermissionService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

public abstract class TaskListControllerTest extends AbstractControllerTest {

  @Autowired
  protected PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  protected ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  protected TaskListService taskListService;

  @MockBean
  protected TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator;

  @MockBean
  protected TaskListEntryFactory taskListEntryFactory;

  @MockBean
  protected ApplicationTaskService applicationTaskService;

  @MockBean(name = "contactServiceForTaskListService")
  protected PwaContactService pwaContactService;

  @MockBean
  protected PadPipelineService padPipelineService;

  @MockBean
  protected PadFileService padFileService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

  @MockBean
  private MasterPwaViewService masterPwaViewService;

  @MockBean
  private PwaHolderTeamService pwaHolderTeamService;

  @MockBean
  protected PwaApplicationPermissionService pwaApplicationPermissionService;

  @Mock
  private MasterPwaView masterPwaView;

  @Mock
  private ApplicationFormSectionService applicationFormSectionService;

  @Mock
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @Mock
  private ApproveOptionsService approveOptionsService;

  @MockBean
  protected MetricsProvider metricsProvider;

  @MockBean
  private Appender appender;

  @Mock
  private PwaAppNotificationBannerService pwaAppNotificationBannerService;

  @Before
  public void taskListControllerTestSetup() {

    when(masterPwaView.getReference()).thenReturn("EXAMPLE_REFERENCE");
    when(masterPwaViewService.getCurrentMasterPwaView(any())).thenReturn(masterPwaView);

    taskListControllerModelAndViewCreator = new TaskListControllerModelAndViewCreator(
        applicationBreadcrumbService,
        taskListEntryFactory,
        masterPwaViewService,
        applicationUpdateRequestViewService,
        approveOptionsService,
        pwaApplicationDetailService,
        pwaAppNotificationBannerService);

    doCallRealMethod().when(applicationBreadcrumbService).fromWorkArea(any(ModelAndView.class), eq("Task list"));

    var taskListTimer = TimerMetricTestUtils.setupTimerMetric(
        TaskListService.class, "pwa.taskListTimer", appender);
    when(metricsProvider.getTaskListTimer()).thenReturn(taskListTimer);

    var appContextTimer = TimerMetricTestUtils.setupTimerMetric(
        PwaApplicationContextService.class, "pwa.appContextTimer", appender);
    when(metricsProvider.getAppContextTimer()).thenReturn(appContextTimer);

  }

}
