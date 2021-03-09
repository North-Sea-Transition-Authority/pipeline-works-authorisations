package uk.co.ogauthority.pwa.service.appprocessing.processingcharges;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CancelPaymentRequestAppProcessingServiceTest {

  @Mock
  private ApplicationChargeRequestService applicationChargeRequestService;

  private CancelPaymentRequestAppProcessingService cancelPaymentRequestAppProcessingService;

  private PwaAppProcessingContext processingContext;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() throws Exception {
    cancelPaymentRequestAppProcessingService = new CancelPaymentRequestAppProcessingService(applicationChargeRequestService);

    when(applicationChargeRequestService.applicationHasOpenChargeRequest(any())).thenReturn(true);

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
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplication)
    );
  }

  @Test
  public void taskAccessible_whenOpenPaymentRequestForApplication_andHasCancelPaymentPermission() {
    assertThat(cancelPaymentRequestAppProcessingService.taskAccessible(processingContext)).isTrue();
  }

  @Test
  public void taskAccessible_whenOpenPaymentRequestForApplication_andHasNoCancelPaymentPermission() {
    processingContext = getContextWithPermissions();
    assertThat(cancelPaymentRequestAppProcessingService.taskAccessible(processingContext)).isFalse();
  }

  @Test
  public void taskAccessible_whenNoOpenPaymentRequestForApplication() {
    when(applicationChargeRequestService.applicationHasOpenChargeRequest(any())).thenReturn(false);
    assertThat(cancelPaymentRequestAppProcessingService.taskAccessible(processingContext)).isFalse();

  }


  @Test
  public void canShowInTaskList_whenOpenPaymentRequestForApplication_andHasCancelPaymentPermission() {
    assertThat(cancelPaymentRequestAppProcessingService.canShowInTaskList(processingContext)).isTrue();
  }

  @Test
  public void canShowInTaskList_whenOpenPaymentRequestForApplication_andHasNoCancelPaymentPermission() {
    processingContext = getContextWithPermissions();
    assertThat(cancelPaymentRequestAppProcessingService.canShowInTaskList(processingContext)).isFalse();
  }

  @Test
  public void canShowInTaskList_whenNoOpenPaymentRequestForApplication() {
    when(applicationChargeRequestService.applicationHasOpenChargeRequest(any())).thenReturn(false);
    assertThat(cancelPaymentRequestAppProcessingService.canShowInTaskList(processingContext)).isFalse();

  }
}