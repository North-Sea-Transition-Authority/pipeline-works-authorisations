package uk.co.ogauthority.pwa.features.application.tasklist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission.ReviewAndSubmitController;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TaskListEntryFactoryTest {

  private final String DEFAULT_APP_TASK_ROUTE = "/My_Task/route";
  private final int DEFAULT_DISPLAY_ORDER = 500;
  private final String DEFAULT_DISPLAY_NAME = "NAME";
  private final String DEFAULT_TASK_LIST_ROUTE = "/Task_list/route";

  @Mock
  private ApplicationTaskService applicationTaskService;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  private TaskListEntryFactory taskListEntryFactory;

  private PwaApplicationDetail pwaApplicationDetail;
  private TestGeneralPurposeApplicationTask testGeneralPurposeApplicationTask;

  @Before
  public void setup() {
    testGeneralPurposeApplicationTask = new TestGeneralPurposeApplicationTask();
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationRedirectService.getTaskListRoute(eq(pwaApplicationDetail.getPwaApplication())))
        .thenReturn(DEFAULT_TASK_LIST_ROUTE);

    taskListEntryFactory = new TaskListEntryFactory(applicationTaskService, pwaApplicationRedirectService);

  }


  @Test
  public void createApplicationTaskListEntry_whenNotComplete_andNoTaskInfoItems() {

    var taskListEntry = taskListEntryFactory.createApplicationTaskListEntry(pwaApplicationDetail, testGeneralPurposeApplicationTask);
    assertThat(taskListEntry.isCompleted()).isFalse();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(DEFAULT_DISPLAY_ORDER);
    assertThat(taskListEntry.getTaskName()).isEqualTo(DEFAULT_DISPLAY_NAME);
    assertThat(taskListEntry.getRoute()).isEqualTo(DEFAULT_APP_TASK_ROUTE);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void createApplicationTaskListEntry_whenComplete_andTaskInfoItems() {

    when(applicationTaskService.isTaskComplete(
        eq(testGeneralPurposeApplicationTask),
        eq(pwaApplicationDetail))
    ).thenReturn(true);

    var taskInfo = new TaskInfo("TYPE", 1L);

    when(applicationTaskService.getTaskInfoList(
        eq(testGeneralPurposeApplicationTask),
        eq(pwaApplicationDetail))
    ).thenReturn(List.of(taskInfo));

    var taskListEntry = taskListEntryFactory.createApplicationTaskListEntry(pwaApplicationDetail, testGeneralPurposeApplicationTask);
    assertThat(taskListEntry.isCompleted()).isTrue();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(DEFAULT_DISPLAY_ORDER);
    assertThat(taskListEntry.getTaskName()).isEqualTo(DEFAULT_DISPLAY_NAME);
    assertThat(taskListEntry.getRoute()).isEqualTo(DEFAULT_APP_TASK_ROUTE);
    assertThat(taskListEntry.getTaskInfoList()).containsExactly(taskInfo);


  }

  @Test
  public void createNoTasksEntry_createdObjectHasExpectedAttributes() {
    var taskListEntry = taskListEntryFactory.createNoTasksEntry(pwaApplicationDetail.getPwaApplication());
    assertThat(taskListEntry.isCompleted()).isFalse();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(0);
    assertThat(taskListEntry.getTaskName()).isEqualTo("No tasks");
    assertThat(taskListEntry.getRoute()).isEqualTo(DEFAULT_TASK_LIST_ROUTE);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
  }

  @Test
  public void createReviewAndSubmitTask_createdObjectHasExpectedAttributes() {
    var taskListEntry = taskListEntryFactory.createReviewAndSubmitTask(pwaApplicationDetail);
    assertThat(taskListEntry.isCompleted()).isFalse();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(999);
    assertThat(taskListEntry.getTaskName()).isEqualTo("Review and submit application");
    assertThat(taskListEntry.getRoute()).isEqualTo(
        ReverseRouter.route(on(ReviewAndSubmitController.class)
            .review(
                pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(),
                null, null)
        )
    );
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
  }

  /**
   * class for use in tests only which implements the generic interface required by class under test
   */
  private class TestGeneralPurposeApplicationTask implements GeneralPurposeApplicationTask {
    @Override
    public Class<? extends ApplicationFormSectionService> getServiceClass() {
      return ApplicationFormSectionService.class;
    }

    @Override
    public Class getControllerClass() {
      return null;
    }

    @Override
    public int getDisplayOrder() {
      return DEFAULT_DISPLAY_ORDER;
    }

    @Override
    public String getDisplayName() {
      return DEFAULT_DISPLAY_NAME;
    }

    @Override
    public String getShortenedDisplayName() {
      return null;
    }

    @Override
    public String getTaskLandingPageRoute(PwaApplication pwaApplication) {
      return DEFAULT_APP_TASK_ROUTE;
    }
  }
}