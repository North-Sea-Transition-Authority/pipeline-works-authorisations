package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask.CHANGE_OPTIONS_APPROVAL_DEADLINE;

import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ChangeOptionsApprovalDeadlineTaskServiceTest {

  @Mock
  private ApproveOptionsService approveOptionsService;


  private ChangeOptionsApprovalDeadlineTaskService changeOptionsApprovalDeadlineTaskService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaAppProcessingContext pwaAppProcessingContext;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.allOf(PwaAppProcessingPermission.class)
    );

    changeOptionsApprovalDeadlineTaskService = new ChangeOptionsApprovalDeadlineTaskService(approveOptionsService);
  }

  @Test
  public void canShowInTaskList_appNotEnded_hasPermission_true() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        Set.of(PwaAppProcessingPermission.CHANGE_OPTIONS_APPROVAL_DEADLINE));

    assertThat(changeOptionsApprovalDeadlineTaskService.canShowInTaskList(pwaAppProcessingContext)).isTrue();
  }

  @Test
  public void canShowInTaskList_appNotEnded_doesNotHavePermission_false() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.noneOf(PwaAppProcessingPermission.class)
    );

    assertThat(changeOptionsApprovalDeadlineTaskService.canShowInTaskList(pwaAppProcessingContext)).isFalse();
  }

  @Test
  public void canShowInTaskList_appEnded_false() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.COMPLETE);
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.noneOf(PwaAppProcessingPermission.class)
    );

    assertThat(changeOptionsApprovalDeadlineTaskService.canShowInTaskList(pwaAppProcessingContext)).isFalse();
  }

  @Test
  public void getTaskListEntry_whenOptionsApproved() {

    when(approveOptionsService.optionsApproved(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(true);

    var taskListEntry = changeOptionsApprovalDeadlineTaskService.getTaskListEntry(
        CHANGE_OPTIONS_APPROVAL_DEADLINE,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getRoute()).isNotNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(CHANGE_OPTIONS_APPROVAL_DEADLINE.getDisplayOrder());
    assertThat(taskListEntry.getTaskName()).isEqualTo(CHANGE_OPTIONS_APPROVAL_DEADLINE.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskTag()).isNull();

  }

  @Test
  public void getTaskListEntry_whenOptionsNotApproved() {

    when(approveOptionsService.optionsApproved(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(false);

    var taskListEntry = changeOptionsApprovalDeadlineTaskService.getTaskListEntry(
        CHANGE_OPTIONS_APPROVAL_DEADLINE,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getRoute()).isNotNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(CHANGE_OPTIONS_APPROVAL_DEADLINE.getDisplayOrder());
    assertThat(taskListEntry.getTaskName()).isEqualTo(CHANGE_OPTIONS_APPROVAL_DEADLINE.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualToIgnoringCase("cannot start yet");

  }

  @Test
  public void taskAccessible_whenHasPermissions_andOptionsApproved() {
    when(approveOptionsService.optionsApproved(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(true);

    assertThat(changeOptionsApprovalDeadlineTaskService.taskAccessible(pwaAppProcessingContext)).isTrue();
  }

  @Test
  public void taskAccessible_whenHasPermissions_andOptionsNotApproved() {
    when(approveOptionsService.optionsApproved(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(false);

    assertThat(changeOptionsApprovalDeadlineTaskService.taskAccessible(pwaAppProcessingContext)).isFalse();
  }

  @Test
  public void taskAccessible_whenNoPermissions() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.noneOf(PwaAppProcessingPermission.class)
    );

    assertThat(changeOptionsApprovalDeadlineTaskService.taskAccessible(pwaAppProcessingContext)).isFalse();
  }
}