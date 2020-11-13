package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.service.consultations.ApplicationConsultationStatusViewTestUtil;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApproveOptionsServiceTest {

  @Mock
  private ConsultationRequestService consultationRequestService;

  private ApproveOptionsService approveOptionsService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaAppProcessingContext pwaAppProcessingContext;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.allOf(PwaAppProcessingPermission.class)
    );

    approveOptionsService = new ApproveOptionsService(
        consultationRequestService
    );

  }

  @Test
  public void canShowInTaskList_whenHasApproveOptionsPermission() {

    assertThat(approveOptionsService.canShowInTaskList(pwaAppProcessingContext)).isTrue();

  }

  @Test
  public void canShowInTaskList_whenApproveOptionsPermissionMissing() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.noneOf(PwaAppProcessingPermission.class)
    );

    assertThat(approveOptionsService.canShowInTaskList(pwaAppProcessingContext)).isFalse();

  }

  @Test
  public void taskAccessible_missingPermission_serviceInteractions() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.noneOf(PwaAppProcessingPermission.class)
    );

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    verifyNoInteractions(consultationRequestService);

  }

  @Test
  public void taskAccessible_hasPermission_serviceInteractions() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(ApplicationConsultationStatusViewTestUtil.noConsultationRequests());

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    verify(consultationRequestService, times(1))
        .getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication());

  }

  @Test
  public void taskAccessible_hasPermission_noConsultationRequest() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(ApplicationConsultationStatusViewTestUtil.noConsultationRequests());

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  public void taskAccessible_hasPermission_hasRespondedRequests_hasUnrespondedRequests() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    var statusView = ApplicationConsultationStatusViewTestUtil.from(List.of(
        ImmutablePair.of(ConsultationRequestStatus.RESPONDED, 1L),
        ImmutablePair.of(ConsultationRequestStatus.AWAITING_RESPONSE, 1L)
    ));

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(statusView);

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  public void taskAccessible_hasPermission_hasRespondedRequests_NoUnrespondedRequests() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    var statusView = ApplicationConsultationStatusViewTestUtil.from(List.of(
        ImmutablePair.of(ConsultationRequestStatus.RESPONDED, 1L),
        ImmutablePair.of(ConsultationRequestStatus.WITHDRAWN, 1L)
    ));

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(statusView);

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    assertThat(taskAccessible).isTrue();

  }
}