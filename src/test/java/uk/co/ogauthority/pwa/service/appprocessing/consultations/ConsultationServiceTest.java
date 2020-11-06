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
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsultationServiceTest {

  @Mock
  private ConsultationRequestService consultationRequestService;

  private ConsultationService consultationService;

  @Before
  public void setUp() {
    consultationService = new ConsultationService(consultationRequestService);
  }

  @Test
  public void canShowInTaskList_viewAllConsultations() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS), null,
        null);

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null,
        null);

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = consultationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_noConsultations() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    when(consultationRequestService.getAllRequestsByApplication(any())).thenReturn(List.of());

    var taskListEntry = consultationService.getTaskListEntry(PwaAppProcessingTask.CONSULTATIONS, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
    assertThat(taskListEntry.getTaskStatus()).isEqualTo(TaskStatus.NOT_STARTED);

  }

  @Test
  public void getTaskListEntry_activeConsultations() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    var activeRequest = new ConsultationRequest();
    activeRequest.setStatus(ConsultationRequestStatus.ALLOCATION);
    when(consultationRequestService.getAllRequestsByApplication(any())).thenReturn(List.of(activeRequest));

    var taskListEntry = consultationService.getTaskListEntry(PwaAppProcessingTask.CONSULTATIONS, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
    assertThat(taskListEntry.getTaskStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

  }

  @Test
  public void getTaskListEntry_allCompletedConsultations() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    var respondedRequest = new ConsultationRequest();
    respondedRequest.setStatus(ConsultationRequestStatus.RESPONDED);

    var withdrawnRequest = new ConsultationRequest();
    withdrawnRequest.setStatus(ConsultationRequestStatus.WITHDRAWN);

    when(consultationRequestService.getAllRequestsByApplication(any())).thenReturn(List.of(respondedRequest, withdrawnRequest));

    var taskListEntry = consultationService.getTaskListEntry(PwaAppProcessingTask.CONSULTATIONS, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
    assertThat(taskListEntry.getTaskStatus()).isEqualTo(TaskStatus.COMPLETED);

  }

}
