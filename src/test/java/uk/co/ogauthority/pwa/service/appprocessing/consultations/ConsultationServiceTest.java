package uk.co.ogauthority.pwa.service.appprocessing.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsultationServiceTest {

  @Mock
  private ConsultationRequestService consultationRequestService;

  private ConsultationService consultationService;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private ApplicationInvolvementDto appInvolvementDto;



  @Before
  public void setUp() {
    consultationService = new ConsultationService(consultationRequestService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    pwaApplication = pwaApplicationDetail.getPwaApplication();

    appInvolvementDto = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        pwaApplication, Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));
  }

  @Test
  public void canShowInTaskList_viewAllConsultations() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS), null, null,
        Set.of());

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null,
        Set.of());

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_showAllTasksPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY), null, null,
        Set.of());

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null, Set.of());

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_notSatisfactory() {

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
  public void getTaskListEntry_noConsultations() {

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
  public void getTaskListEntry_activeConsultations() {

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
  public void getTaskListEntry_allCompletedConsultations() {

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
  public void getTaskListEntry_hasViewPermissionAndCorrectAppStatus_noSatisfactoryVersion_taskStateLocked() {

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplication);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.LOCK);
  }

  @Test
  public void getTaskListEntry_hasViewPermissionAndSatisfactoryVersion_incorrectAppStatus_taskStateLocked() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.LOCK);
  }

  @Test
  public void getTaskListEntry_hasCorrectAppStatusAndSatisfactoryVersion_noViewPermission_taskStateLocked() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.LOCK);
  }

  @Test
  public void getTaskListEntry_hasCorrectAppStatusAndSatisfactoryVersionAndViewPermission_taskStateView() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.VIEW);
  }

  @Test
  public void getTaskListEntry_hasCorrectAppStatusAndSatisfactoryVersionAndEditPermission_taskStateEdit() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.EDIT_CONSULTATIONS), null, appInvolvementDto, Set.of());

    assertThat(consultationService.getTaskState(processingContext)).isEqualTo(TaskState.EDIT);
  }




}
