package uk.co.ogauthority.pwa.features.application.tasklist.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListEntryFactory;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.model.view.banner.PageBannerView;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaAppNotificationBannerService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TaskListControllerModelAndViewCreatorTest {

  @Mock
  private ApplicationBreadcrumbService breadcrumbService;

  @Mock
  private TaskListEntryFactory taskListEntryFactory;

  @Mock
  private MasterPwaViewService masterPwaViewService;

  @Mock
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @Mock
  private ApproveOptionsService approveOptionsService;

  @Mock
  private PwaAppNotificationBannerService pwaAppNotificationBannerService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator;

  private PwaApplicationDetail detail;

  private List<TaskListGroup> taskListGroups;

  @Before
  public void setUp() throws Exception {
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    taskListGroups = List.of(new TaskListGroup("Group 1", 1, Collections.emptyList()));

    taskListControllerModelAndViewCreator = new TaskListControllerModelAndViewCreator(breadcrumbService,
        taskListEntryFactory,
        masterPwaViewService,
        applicationUpdateRequestViewService,
        approveOptionsService,
        pwaApplicationDetailService,
        pwaAppNotificationBannerService);
  }


  @Test
  public void getTaskListModelAndView_generic_firstVersion() {

    var masterPwaView = mock(MasterPwaView.class);
    when(masterPwaView.getReference()).thenReturn("PWA-Example");

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(pwaApplication);
    detail.setVersionNo(1);

    when(masterPwaViewService.getCurrentMasterPwaView(pwaApplication)).thenReturn(masterPwaView);

    PwaApplicationType.stream().forEach(applicationType -> {

      pwaApplication.setApplicationType(applicationType);

      var modelAndView = taskListControllerModelAndViewCreator.getTaskListModelAndView(detail, taskListGroups);

      assertThat(modelAndView.getViewName()).isEqualTo(TaskListControllerModelAndViewCreator.TASK_LIST_TEMPLATE_PATH);

      assertThat(modelAndView.getModel().get("applicationTaskGroups")).isNotNull();

      if (applicationType != PwaApplicationType.INITIAL) {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isEqualTo("PWA-Example");
      } else {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isNull();
      }

      verify(breadcrumbService, times(1)).fromWorkArea(modelAndView, "Task list");

      verifyNoInteractions(applicationUpdateRequestViewService);

    });

  }

  @Test
  public void getTaskListModelAndView_notFirstVersion_appUpdateOpen() {

    var masterPwaView = mock(MasterPwaView.class);
    when(masterPwaView.getReference()).thenReturn("PWA-Example");

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(pwaApplication);
    detail.setVersionNo(2);

    when(masterPwaViewService.getCurrentMasterPwaView(pwaApplication)).thenReturn(masterPwaView);

    var updateRequestView = mock(ApplicationUpdateRequestView.class);
    when(applicationUpdateRequestViewService.getOpenRequestView(any(PwaApplication.class))).thenReturn(Optional.of(updateRequestView));

    PwaApplicationType.stream().forEach(applicationType -> {

      pwaApplication.setApplicationType(applicationType);

      var modelAndView = taskListControllerModelAndViewCreator.getTaskListModelAndView(detail, taskListGroups);

      assertThat(modelAndView.getViewName()).isEqualTo(TaskListControllerModelAndViewCreator.TASK_LIST_TEMPLATE_PATH);

      assertThat(modelAndView.getModel().get("applicationTaskGroups")).isNotNull();

      if (applicationType != PwaApplicationType.INITIAL) {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isEqualTo("PWA-Example");
      } else {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isNull();
      }

      verify(breadcrumbService, times(1)).fromCaseManagement(pwaApplication, modelAndView, "Task list");

      assertThat(modelAndView.getModel().get("updateRequestView")).isEqualTo(updateRequestView);

    });

  }

  @Test
  public void getTaskListModelAndView_notFirstVersion_noAppUpdateOpen() {

    var masterPwaView = mock(MasterPwaView.class);
    when(masterPwaView.getReference()).thenReturn("PWA-Example");

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(pwaApplication);
    detail.setVersionNo(2);

    when(masterPwaViewService.getCurrentMasterPwaView(pwaApplication)).thenReturn(masterPwaView);

    when(applicationUpdateRequestViewService.getOpenRequestView(any(PwaApplication.class))).thenReturn(Optional.empty());

    PwaApplicationType.stream().forEach(applicationType -> {

      pwaApplication.setApplicationType(applicationType);

      var modelAndView = taskListControllerModelAndViewCreator.getTaskListModelAndView(detail, taskListGroups);

      assertThat(modelAndView.getViewName()).isEqualTo(TaskListControllerModelAndViewCreator.TASK_LIST_TEMPLATE_PATH);

      assertThat(modelAndView.getModel().get("applicationTaskGroups")).isNotNull();

      if (applicationType != PwaApplicationType.INITIAL) {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isEqualTo("PWA-Example");
      } else {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isNull();
      }

      verify(breadcrumbService, times(1)).fromCaseManagement(pwaApplication, modelAndView, "Task list");

      assertThat(modelAndView.getModel().get("updateRequestView")).isNull();

    });

  }

  @Test
  public void getTaskListModelAndView_optionsApprovedBannerProvided() {

    var masterPwaView = mock(MasterPwaView.class);
    when(masterPwaView.getReference()).thenReturn("PWA-Example");

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    var pwaApplication = detail.getPwaApplication();
    detail.setVersionNo(2);

    var optionsPageBanner = new PageBannerView.PageBannerViewBuilder().build();

    when(masterPwaViewService.getCurrentMasterPwaView(pwaApplication)).thenReturn(masterPwaView);

    when(applicationUpdateRequestViewService.getOpenRequestView(any(PwaApplication.class)))
        .thenReturn(Optional.empty());
    when(approveOptionsService.getOptionsApprovalPageBannerView(any())).thenReturn(Optional.of(optionsPageBanner));

    var modelAndView = taskListControllerModelAndViewCreator.getTaskListModelAndView(detail, taskListGroups);

    assertThat(modelAndView.getModel().get("applicationTaskGroups")).isNotNull();

    assertThat(modelAndView.getModel().get("updateRequestView")).isNull();
    assertThat(modelAndView.getModel().get("optionsApprovalPageBanner")).isEqualTo(optionsPageBanner);


  }
}