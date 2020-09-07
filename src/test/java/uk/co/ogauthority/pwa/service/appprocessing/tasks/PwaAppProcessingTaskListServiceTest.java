package uk.co.ogauthority.pwa.service.appprocessing.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskRequirement;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTaskGroup;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaAppProcessingTaskListServiceTest {

  private final ApplicationTask DEFAULT_APP_TASK = ApplicationTask.FIELD_INFORMATION;
  private final ApplicationTaskGroup DEFAULT_APP_TASK_GROUP = ApplicationTaskGroup.ADMINISTRATIVE_DETAILS;

  @Mock
  private PwaAppProcessingTaskService processingTaskService;

  private PwaAppProcessingTaskListService taskListService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaAppProcessingContext processingContext;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(), null);

    taskListService = new PwaAppProcessingTaskListService(processingTaskService);

  }

  @Test
  public void getTaskListGroups() {

    when(processingTaskService.canShowTask(any(), any())).thenReturn(true);

    var taskListGroups = taskListService.getTaskListGroups(processingContext);

    assertThat(taskListGroups)
        .extracting(TaskListGroup::getGroupName, TaskListGroup::getDisplayOrder)
        .containsExactly(
            tuple(TaskRequirement.REQUIRED.getDisplayName(), TaskRequirement.REQUIRED.getDisplayOrder()),
            tuple(TaskRequirement.OPTIONAL.getDisplayName(), TaskRequirement.OPTIONAL.getDisplayOrder())
        );

    assertThat(taskListGroups.get(0).getTaskListEntries())
        .extracting(TaskListEntry::getTaskName, TaskListEntry::getRoute)
        .containsExactly(
            tuple(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName(), PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.ACCEPT_APPLICATION.getTaskName(), PwaAppProcessingTask.ACCEPT_APPLICATION.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.CASE_SETUP.getTaskName(), PwaAppProcessingTask.CASE_SETUP.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.CONSULTATIONS.getTaskName(), PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName(), PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.DECISION.getTaskName(), PwaAppProcessingTask.DECISION.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName(), PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.CONSULTATION_ADVICE.getTaskName(), PwaAppProcessingTask.CONSULTATION_ADVICE.getRoute(processingContext.getApplicationDetail()))
        );

    assertThat(taskListGroups.get(1).getTaskListEntries())
        .extracting(TaskListEntry::getTaskName, TaskListEntry::getRoute)
        .containsExactly(
            tuple(PwaAppProcessingTask.ALLOCATE_CASE_OFFICER.getTaskName(), PwaAppProcessingTask.ALLOCATE_CASE_OFFICER.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.RFI.getTaskName(), PwaAppProcessingTask.RFI.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.ADD_NOTE_OR_DOCUMENT.getTaskName(), PwaAppProcessingTask.ADD_NOTE_OR_DOCUMENT.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.WITHDRAW_APPLICATION.getTaskName(), PwaAppProcessingTask.WITHDRAW_APPLICATION.getRoute(processingContext.getApplicationDetail()))
        );

  }

  @Test
  public void getTaskListGroups_noOptional() {

    PwaAppProcessingTask.stream().forEach(task ->
        when(processingTaskService.canShowTask(task, processingContext)).thenReturn(task.getTaskRequirement().equals(
            TaskRequirement.REQUIRED)));

    var taskListGroups = taskListService.getTaskListGroups(processingContext);

    assertThat(taskListGroups)
        .extracting(TaskListGroup::getGroupName, TaskListGroup::getDisplayOrder)
        .containsExactly(
            tuple(TaskRequirement.REQUIRED.getDisplayName(), TaskRequirement.REQUIRED.getDisplayOrder())
        );

    assertThat(taskListGroups.get(0).getTaskListEntries())
        .extracting(TaskListEntry::getTaskName, TaskListEntry::getRoute)
        .containsExactly(
            tuple(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName(), PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.ACCEPT_APPLICATION.getTaskName(), PwaAppProcessingTask.ACCEPT_APPLICATION.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.CASE_SETUP.getTaskName(), PwaAppProcessingTask.CASE_SETUP.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.CONSULTATIONS.getTaskName(), PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName(), PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.DECISION.getTaskName(), PwaAppProcessingTask.DECISION.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName(), PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext.getApplicationDetail())),
            tuple(PwaAppProcessingTask.CONSULTATION_ADVICE.getTaskName(), PwaAppProcessingTask.CONSULTATION_ADVICE.getRoute(processingContext.getApplicationDetail()))
        );


  }

}
