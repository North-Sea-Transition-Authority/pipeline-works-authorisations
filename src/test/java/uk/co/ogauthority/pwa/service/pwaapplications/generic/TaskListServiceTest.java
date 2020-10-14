package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTaskGroup;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TaskListServiceTest {
  private final ApplicationTask DEFAULT_APP_TASK = ApplicationTask.FIELD_INFORMATION;
  private final ApplicationTaskGroup DEFAULT_APP_TASK_GROUP = ApplicationTaskGroup.ADMINISTRATIVE_DETAILS;

  @Mock
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @Mock
  private TaskListEntryFactory taskListEntryFactory;

  @Mock
  private MasterPwaViewService masterPwaViewService;

  @Mock
  private ApplicationTaskService applicationTaskService;

  @Mock
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  private TaskListService taskListService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    taskListService = new TaskListService(
        applicationBreadcrumbService,
        taskListEntryFactory,
        applicationTaskService,
        masterPwaViewService,
        applicationUpdateRequestViewService);
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

      var modelAndView = taskListService.getTaskListModelAndView(detail);

      assertThat(modelAndView.getViewName()).isEqualTo(TaskListService.TASK_LIST_TEMPLATE_PATH);

      assertThat(modelAndView.getModel().get("applicationTaskGroups")).isNotNull();

      if (applicationType != PwaApplicationType.INITIAL) {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isEqualTo("PWA-Example");
      } else {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isNull();
      }

      verify(applicationBreadcrumbService, times(1)).fromWorkArea(modelAndView, "Task list");

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

      var modelAndView = taskListService.getTaskListModelAndView(detail);

      assertThat(modelAndView.getViewName()).isEqualTo(TaskListService.TASK_LIST_TEMPLATE_PATH);

      assertThat(modelAndView.getModel().get("applicationTaskGroups")).isNotNull();

      if (applicationType != PwaApplicationType.INITIAL) {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isEqualTo("PWA-Example");
      } else {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isNull();
      }

      verify(applicationBreadcrumbService, times(1)).fromCaseManagement(pwaApplication, modelAndView, "Task list");

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

      var modelAndView = taskListService.getTaskListModelAndView(detail);

      assertThat(modelAndView.getViewName()).isEqualTo(TaskListService.TASK_LIST_TEMPLATE_PATH);

      assertThat(modelAndView.getModel().get("applicationTaskGroups")).isNotNull();

      if (applicationType != PwaApplicationType.INITIAL) {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isEqualTo("PWA-Example");
      } else {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isNull();
      }

      verify(applicationBreadcrumbService, times(1)).fromCaseManagement(pwaApplication, modelAndView, "Task list");

      assertThat(modelAndView.getModel().get("updateRequestView")).isNull();

    });

  }

  @Test
  public void anyTaskShownForApplication_whenMultiple_andNeitherShown() {

    var projTask = ApplicationTask.PROJECT_INFORMATION;
    var permTask = ApplicationTask.PERMANENT_DEPOSITS;

    when(applicationTaskService.canShowTask(projTask, pwaApplicationDetail)).thenReturn(false);
    when(applicationTaskService.canShowTask(permTask, pwaApplicationDetail)).thenReturn(false);

    var result = taskListService.anyTaskShownForApplication(
        Set.of(projTask, permTask),
        pwaApplicationDetail
    );

    assertThat(result).isFalse();

  }

  @Test
  public void anyTaskShownForApplication_whenMultiple_andOnlyOneShown() {

    var projTask = ApplicationTask.PROJECT_INFORMATION;
    var permTask = ApplicationTask.PERMANENT_DEPOSITS;

    when(applicationTaskService.canShowTask(projTask, pwaApplicationDetail)).thenReturn(true);
    var result = taskListService.anyTaskShownForApplication(
        Set.of(projTask, permTask),
        pwaApplicationDetail
    );

    assertThat(result).isTrue();

  }

  @Test
  public void getShownApplicationTasksForDetail_allTasksQueried() {
    taskListService.getApplicationTaskListEntries(pwaApplicationDetail);

    ApplicationTask.stream().forEach(applicationTask -> {
      verify(applicationTaskService, times(1)).canShowTask(applicationTask, pwaApplicationDetail);
    });

  }

  @Test
  public void getApplicationTaskListEntries_whenNoTasksCanBeShown_thenReturnNoTasksItem() {
    var fakeNoTasksTaskListEntry = new TaskListEntry("fake name", "fake route", false, 0);
    when(taskListEntryFactory.createNoTasksEntry(pwaApplicationDetail.getPwaApplication())).thenReturn(
        fakeNoTasksTaskListEntry);

    var taskListEntries = taskListService.getApplicationTaskListEntries(pwaApplicationDetail);

    assertThat(taskListEntries).containsExactly(fakeNoTasksTaskListEntry);

  }

  @Test
  public void getApplicationTaskListEntries_whenSomeTaskCanBeShown_thenOnlyReturnThatTaskListEntry() {
    var fakeTaskListEntry = new TaskListEntry("fake name", "fake route", false, 0);
    when(applicationTaskService.canShowTask(DEFAULT_APP_TASK, pwaApplicationDetail)).thenReturn(true);
    when(taskListEntryFactory.createApplicationTaskListEntry(any(), any())).thenReturn(fakeTaskListEntry);

    var taskListEntries = taskListService.getApplicationTaskListEntries(pwaApplicationDetail);

    assertThat(taskListEntries).containsExactly(fakeTaskListEntry);
    verify(taskListEntryFactory, times(1)).createApplicationTaskListEntry(pwaApplicationDetail, DEFAULT_APP_TASK);
    verifyNoMoreInteractions(taskListEntryFactory);

  }

  @Test
  public void getTaskListGroups_whenNoApplicationTasksShown() {
    assertThat(taskListService.getTaskListGroups(pwaApplicationDetail).isEmpty());
  }

  @Test
  public void getTaskListGroups_whenOneTaskInGroupIsShown() {
    var fakeTaskListEntry = new TaskListEntry("fake name", "fake route", false, 0);
    when(taskListEntryFactory.createApplicationTaskListEntry(any(), any())).thenReturn(fakeTaskListEntry);
    when(applicationTaskService.canShowTask(DEFAULT_APP_TASK, pwaApplicationDetail)).thenReturn(true);

    var taskListGroups = taskListService.getTaskListGroups(pwaApplicationDetail);

    assertThat(taskListGroups).hasSize(1);
    assertThat(taskListGroups.get(0)).satisfies(taskListGroup -> {
      assertThat(taskListGroup.getGroupName()).isEqualTo(DEFAULT_APP_TASK_GROUP.getDisplayName());
      assertThat(taskListGroup.getDisplayOrder()).isEqualTo(DEFAULT_APP_TASK_GROUP.getDisplayOrder());
      assertThat(taskListGroup.getTaskListEntries()).containsExactly(fakeTaskListEntry);
    });

  }

}
