package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTaskGroup;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TaskListServiceTest {
  private final ApplicationTask DEFAULT_APP_TASK = ApplicationTask.FIELD_INFORMATION;
  private final ApplicationTaskGroup DEFAULT_APP_TASK_GROUP = ApplicationTaskGroup.ADMINISTRATIVE_DETAILS;

  @Mock
  private TaskListEntryFactory taskListEntryFactory;

  @Mock
  private ApplicationTaskService applicationTaskService;

  private TaskListService taskListService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    taskListService = new TaskListService(
        taskListEntryFactory,
        applicationTaskService
    );
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
    assertThat(taskListService.getTaskListGroups(pwaApplicationDetail)).isEmpty();
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
