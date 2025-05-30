package uk.co.ogauthority.pwa.service.appprocessing.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

  @Mock
  private ConsultationRequestService consultationRequestService;

  private ConsultationService consultationService;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private ApplicationInvolvementDto appInvolvementDto;


  @BeforeEach
  void setUp() {
    consultationService = new ConsultationService(consultationRequestService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    pwaApplication = pwaApplicationDetail.getPwaApplication();

    appInvolvementDto = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        pwaApplication, Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));
  }

  @Test
  void canShowInTaskList_viewAllConsultations() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS), null, null,
        Set.of());

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null,
        Set.of());

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_showAllTasksPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY), null, null,
        Set.of());

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_noPermissions() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null, Set.of());

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void getTaskListEntry_notSatisfactory() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication()), Set.of());

    when(consultationRequestService.getAllRequestsByApplication(any())).thenReturn(List.of());

    var taskListEntry = consultationService.getTaskListEntry(PwaAppProcessingTask.CONSULTATIONS, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.CANNOT_START_YET));

  }


  @Test
  void getTaskListEntry_noConsultations() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    when(consultationRequestService.getAllRequestsByApplication(any())).thenReturn(List.of());

    var taskListEntry = consultationService.getTaskListEntry(PwaAppProcessingTask.CONSULTATIONS, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));

  }

  @Test
  void getTaskListEntry_activeConsultations() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    var activeRequest = new ConsultationRequest();
    activeRequest.setStatus(ConsultationRequestStatus.ALLOCATION);
    when(consultationRequestService.getAllRequestsByApplication(any())).thenReturn(List.of(activeRequest));

    var taskListEntry = consultationService.getTaskListEntry(PwaAppProcessingTask.CONSULTATIONS, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.IN_PROGRESS));

  }

  @Test
  void getTaskListEntry_allCompletedConsultations() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    var respondedRequest = new ConsultationRequest();
    respondedRequest.setStatus(ConsultationRequestStatus.RESPONDED);

    var withdrawnRequest = new ConsultationRequest();
    withdrawnRequest.setStatus(ConsultationRequestStatus.WITHDRAWN);

    when(consultationRequestService.getAllRequestsByApplication(any())).thenReturn(List.of(respondedRequest, withdrawnRequest));

    var taskListEntry = consultationService.getTaskListEntry(PwaAppProcessingTask.CONSULTATIONS, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.COMPLETED));

  }

  @Test
  void getTaskListEntry_hasViewPermissionAndCorrectAppStatus_noSatisfactoryVersion_taskStateLocked() {

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplication);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.LOCK);
  }

  @Test
  void getTaskListEntry_hasViewPermissionAndSatisfactoryVersion_incorrectAppStatus_taskStateLocked() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.LOCK);
  }

  @Test
  void getTaskListEntry_hasCorrectAppStatusAndSatisfactoryVersion_noViewPermission_taskStateLocked() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.LOCK);
  }

  @Test
  void getTaskListEntry_hasCorrectAppStatusAndSatisfactoryVersionAndViewPermission_taskStateView() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.VIEW);
  }

  @Test
  void getTaskListEntry_hasCorrectAppStatusAndSatisfactoryVersionAndEditPermission_taskStateEdit() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.EDIT_CONSULTATIONS), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.EDIT);
  }




}
