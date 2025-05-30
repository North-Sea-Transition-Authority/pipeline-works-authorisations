package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class CloseOutOptionsTaskServiceTest {

  private static final PersonId PERSON_ID = new PersonId(1);

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Mock
  private ApproveOptionsService approveOptionsService;

  private CloseOutOptionsTaskService closeOutOptionsTaskService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaAppProcessingContext pwaAppProcessingContext;

  @BeforeEach
  void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.complementOf(EnumSet.of(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY))
    );

    closeOutOptionsTaskService = new CloseOutOptionsTaskService(
        applicationUpdateRequestService,
        approveOptionsService
    );

  }

  @Test
  void canShowInTaskList_withPermissions() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.CLOSE_OUT_OPTIONS)
    );

    assertThat(closeOutOptionsTaskService.canShowInTaskList(pwaAppProcessingContext)).isTrue();
  }

  @Test
  void canShowInTaskList_showAllTasksPermission_optionsAppType() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY)
    );
    assertThat(closeOutOptionsTaskService.canShowInTaskList(pwaAppProcessingContext)).isTrue();
  }

  @Test
  void canShowInTaskList_showAllTasksPermission_notOptionsAppType() {
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        detail,
        EnumSet.of(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY)
    );
    assertThat(closeOutOptionsTaskService.canShowInTaskList(pwaAppProcessingContext)).isFalse();
  }

  @Test
  void canShowInTaskList_withoutPermissions() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.noneOf(PwaAppProcessingPermission.class)
    );

    assertThat(closeOutOptionsTaskService.canShowInTaskList(pwaAppProcessingContext)).isFalse();
  }

  @Test
  void taskAccessible_whenAppComplete_andWithPermissions_andConsentedOptionConfirmed() {
    pwaApplicationDetail.setStatus(PwaApplicationStatus.COMPLETE);

    when(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .thenReturn(OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED);

    assertThat(closeOutOptionsTaskService.taskAccessible(pwaAppProcessingContext)).isFalse();
  }

  @Test
  void taskAccessible_whenAppNotComplete_andWithPermissions_andApprovedOptions_andNoAppUpdate_andOtherOptionConfirmed() {

    when(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .thenReturn(OptionsApprovalStatus.APPROVED_OTHER_CONFIRMED);

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(false);

    assertThat(closeOutOptionsTaskService.taskAccessible(pwaAppProcessingContext)).isTrue();
  }

  @Test
  void taskAccessible_whenAppNotComplete_andWithPermissions_andApprovedOptions_andOpenAppUpdate() {

    when(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .thenReturn(OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED);

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(true);

    assertThat(closeOutOptionsTaskService.taskAccessible(pwaAppProcessingContext)).isFalse();
  }

  @Test
  void taskAccessible_whenAppNotComplete_andWithoutPermissions_andNotApproved() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.noneOf(PwaAppProcessingPermission.class)
    );

    when(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .thenReturn(OptionsApprovalStatus.NOT_APPROVED);

    assertThat(closeOutOptionsTaskService.taskAccessible(pwaAppProcessingContext)).isFalse();
  }

  @Test
  void getTaskListEntry_whenAppComplete_andApprovedOptions_andNoAppUpdate_andConsentedOptionConfirmed() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.COMPLETE);

    when(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .thenReturn(OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED);

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(false);

    var taskListEntry = closeOutOptionsTaskService.getTaskListEntry(
        PwaAppProcessingTask.CLOSE_OUT_OPTIONS,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isNotNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getDisplayOrder());
    Assertions.assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualTo(TaskStatus.NOT_REQUIRED.getDisplayText());

  }

  @Test
  void getTaskListEntry_whenAppNotComplete_andApprovedOptions_andNoAppUpdate_andConsentedOptionConfirmed() {


    when(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .thenReturn(OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED);

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(false);

    var taskListEntry = closeOutOptionsTaskService.getTaskListEntry(
        PwaAppProcessingTask.CLOSE_OUT_OPTIONS,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isNotNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getDisplayOrder());
    Assertions.assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualTo(TaskStatus.NOT_REQUIRED.getDisplayText());

  }

  @Test
  void getTaskListEntry_whenAppNotComplete_andApprovedOptions_andNoAppUpdate_andOtherOptionConfirmed() {


    when(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .thenReturn(OptionsApprovalStatus.APPROVED_OTHER_CONFIRMED);

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(false);

    var taskListEntry = closeOutOptionsTaskService.getTaskListEntry(
        PwaAppProcessingTask.CLOSE_OUT_OPTIONS,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isNotNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getDisplayOrder());
    Assertions.assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualTo(TaskStatus.NOT_STARTED.getDisplayText());
  }

  @Test
  void getTaskListEntry_whenAppNotComplete_andApprovedOptions_andOpenAppUpdate_andOtherOptionConfirmed() {


    when(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .thenReturn(OptionsApprovalStatus.APPROVED_OTHER_CONFIRMED);

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(true);

    var taskListEntry = closeOutOptionsTaskService.getTaskListEntry(
        PwaAppProcessingTask.CLOSE_OUT_OPTIONS,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isNotNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getDisplayOrder());
    Assertions.assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualTo(TaskStatus.CANNOT_START_YET.getDisplayText());
  }
}