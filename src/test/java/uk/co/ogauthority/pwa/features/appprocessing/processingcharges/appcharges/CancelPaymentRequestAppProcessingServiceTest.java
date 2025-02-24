package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class CancelPaymentRequestAppProcessingServiceTest {

  @Mock
  private ApplicationChargeRequestService applicationChargeRequestService;

  private CancelPaymentRequestAppProcessingService cancelPaymentRequestAppProcessingService;

  private PwaAppProcessingContext processingContext;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;


  @BeforeEach
  void setUp() {
    cancelPaymentRequestAppProcessingService = new CancelPaymentRequestAppProcessingService(applicationChargeRequestService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();

    processingContext = getContextWithPermissions(PwaAppProcessingPermission.CANCEL_PAYMENT);


  }

  private PwaAppProcessingContext getContextWithPermissions(PwaAppProcessingPermission ... pwaAppProcessingPermissions){
    var permissionSet = EnumSet.noneOf(PwaAppProcessingPermission.class);
    permissionSet.addAll(Arrays.asList(pwaAppProcessingPermissions));

    return new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        permissionSet,
        null,
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplication), Set.of());
  }

  @Test
  void taskAccessible_whenOpenPaymentRequestForApplication_andHasCancelPaymentPermission() {
    when(applicationChargeRequestService.applicationHasOpenChargeRequest(any())).thenReturn(true);
    assertThat(cancelPaymentRequestAppProcessingService.taskAccessible(processingContext)).isTrue();
  }

  @Test
  void taskAccessible_whenOpenPaymentRequestForApplication_andHasNoCancelPaymentPermission() {
    processingContext = getContextWithPermissions();
    assertThat(cancelPaymentRequestAppProcessingService.taskAccessible(processingContext)).isFalse();
  }

  @Test
  void taskAccessible_whenNoOpenPaymentRequestForApplication() {
    when(applicationChargeRequestService.applicationHasOpenChargeRequest(any())).thenReturn(false);
    assertThat(cancelPaymentRequestAppProcessingService.taskAccessible(processingContext)).isFalse();

  }


  @Test
  void canShowInTaskList_whenOpenPaymentRequestForApplication_andHasCancelPaymentPermission() {
    when(applicationChargeRequestService.applicationHasOpenChargeRequest(any())).thenReturn(true);
    assertThat(cancelPaymentRequestAppProcessingService.canShowInTaskList(processingContext)).isTrue();
  }

  @Test
  void canShowInTaskList_whenOpenPaymentRequestForApplication_andHasNoCancelPaymentPermission() {
    processingContext = getContextWithPermissions();
    assertThat(cancelPaymentRequestAppProcessingService.canShowInTaskList(processingContext)).isFalse();
  }

  @Test
  void canShowInTaskList_whenNoOpenPaymentRequestForApplication() {
    when(applicationChargeRequestService.applicationHasOpenChargeRequest(any())).thenReturn(false);
    assertThat(cancelPaymentRequestAppProcessingService.canShowInTaskList(processingContext)).isFalse();

  }
}