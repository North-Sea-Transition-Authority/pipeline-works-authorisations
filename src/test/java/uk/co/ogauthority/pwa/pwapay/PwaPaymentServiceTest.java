package uk.co.ogauthority.pwa.pwapay;


import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.co.ogauthority.pwa.govukpay.GovUkPayCardPaymentClient;
import uk.co.ogauthority.pwa.govukpay.GovUkPaymentStatus;
import uk.co.ogauthority.pwa.govukpay.PaymentJourneyStateTestUtil;

public class PwaPaymentServiceTest {

  private final Clock clock = Clock.fixed(
      LocalDateTime.of(2020, 12, 1, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault());

  @Mock
  private GovUkPayCardPaymentClient govUkPayCardPaymentClient;

  @Mock
  private PwaPaymentRequestPersister pwaPaymentRequestPersister;

  @Mock
  private PwaPaymentRequestRepository pwaPaymentRequestRepository;

  private PwaPaymentService pwaPaymentService;

  @Before
  public void setUp() throws Exception {

    var contextPath = "applicationContextPath";
    var pwaUrlBase = "pwaUrlBase.com/";

    pwaPaymentService = new PwaPaymentService(
        govUkPayCardPaymentClient,
        pwaPaymentRequestPersister,
        clock,
        pwaPaymentRequestRepository,
        pwaUrlBase,
        contextPath
    );
  }

  @Test
  public void createCardPayment() {
  }

  @Test
  public void getPwaPaymentRequests() {
  }

  @Test
  public void getGovUkPaymentRequestOrError() {
  }

  @Test
  public void refreshPwaPaymentRequestData() {
  }

  @Test
  public void cancelPayment() {



  }

  @Test
  public void decodeGovPayStatus_whenJourneyFailed() {
    assertThat(pwaPaymentService.decodeGovPayStatus(PaymentJourneyStateTestUtil.createFor(GovUkPaymentStatus.FAILED)))
    .isEqualTo(PaymentRequestStatus.COMPLETE_WITHOUT_PAYMENT);
  }

  @Test
  public void decodeGovPayStatus_whenJourneyIsSuccess() {
    assertThat(pwaPaymentService.decodeGovPayStatus(PaymentJourneyStateTestUtil.createFor(GovUkPaymentStatus.SUCCESS)))
        .isEqualTo(PaymentRequestStatus.PAYMENT_COMPLETE);
  }

  @Test
  public void decodeGovPayStatus_whenJourneyIsCancelledByService() {
    assertThat(pwaPaymentService.decodeGovPayStatus(PaymentJourneyStateTestUtil.createFor(GovUkPaymentStatus.CANCELLED)))
        .isEqualTo(PaymentRequestStatus.CANCELLED);
  }

  @Test
  public void decodeGovPayStatus_whenJourneyIsOngoing() {
    var journeyInProgressStatuses = Arrays.stream(GovUkPaymentStatus.values())
        .filter(govUkPaymentStatus-> !govUkPaymentStatus.isJourneyFinished())
        .collect(toSet());

    for(GovUkPaymentStatus govUkPaymentStatus : journeyInProgressStatuses) {
      assertThat(pwaPaymentService.decodeGovPayStatus(PaymentJourneyStateTestUtil.createFor(govUkPaymentStatus)))
          .isEqualTo(PaymentRequestStatus.IN_PROGRESS);
    }
  }
}