package uk.co.ogauthority.pwa.service.pwaapplications.generic;

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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TaskListEntryFactoryTest {

  private final ApplicationTask DEFAULT_APP_TASK = ApplicationTask.APPLICATION_USERS;
  private final String DEFAULT_APP_TASK_ROUTE = "/My_Task/route";
  private final String DEFAULT_TASK_LIST_ROUTE = "/Task_list/route";

  @Mock
  private ApplicationTaskService applicationTaskService;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  private TaskListEntryFactory taskListEntryFactory;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setup() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(applicationTaskService.getRouteForTask(
        eq(DEFAULT_APP_TASK),
        eq(pwaApplicationDetail.getPwaApplicationType()),
        eq(pwaApplicationDetail.getMasterPwaApplicationId()))
    ).thenReturn(DEFAULT_APP_TASK_ROUTE);

    when(pwaApplicationRedirectService.getTaskListRoute(eq(pwaApplicationDetail.getPwaApplication())))
        .thenReturn(DEFAULT_TASK_LIST_ROUTE);

    taskListEntryFactory = new TaskListEntryFactory(applicationTaskService, pwaApplicationRedirectService);

  }


  @Test
  public void createApplicationTaskListEntry_whenNotComplete_andNoTaskInfoItems() {

    var taskListEntry = taskListEntryFactory.createApplicationTaskListEntry(pwaApplicationDetail, DEFAULT_APP_TASK);
    assertThat(taskListEntry.isCompleted()).isFalse();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(DEFAULT_APP_TASK.getDisplayOrder());
    assertThat(taskListEntry.getTaskName()).isEqualTo(DEFAULT_APP_TASK.getDisplayName());
    assertThat(taskListEntry.getRoute()).isEqualTo(DEFAULT_APP_TASK_ROUTE);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();


  }

  @Test
  public void createApplicationTaskListEntry_whenComplete_andTaskInfoItems() {

    when(applicationTaskService.isTaskComplete(
        eq(DEFAULT_APP_TASK),
        eq(pwaApplicationDetail))
    ).thenReturn(true);

    var taskInfo = new TaskInfo("TYPE", 1L);

    when(applicationTaskService.getTaskInfoList(
        eq(DEFAULT_APP_TASK),
        eq(pwaApplicationDetail))
    ).thenReturn(List.of(taskInfo));

    var taskListEntry = taskListEntryFactory.createApplicationTaskListEntry(pwaApplicationDetail, DEFAULT_APP_TASK);
    assertThat(taskListEntry.isCompleted()).isTrue();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(DEFAULT_APP_TASK.getDisplayOrder());
    assertThat(taskListEntry.getTaskName()).isEqualTo(DEFAULT_APP_TASK.getDisplayName());
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
                null)
        )
    );
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
  }
}