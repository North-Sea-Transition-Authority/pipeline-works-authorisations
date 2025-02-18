package uk.co.ogauthority.pwa.features.pwapay;


import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.integrations.govukpay.GovPayNewCardPaymentRequest;
import uk.co.ogauthority.pwa.integrations.govukpay.GovPayNewCardPaymentResultTestUtil;
import uk.co.ogauthority.pwa.integrations.govukpay.GovPayPaymentJourneyDataTestUtil;
import uk.co.ogauthority.pwa.integrations.govukpay.GovPayPaymentJourneyStateTestUtil;
import uk.co.ogauthority.pwa.integrations.govukpay.GovUkPayCardPaymentClient;
import uk.co.ogauthority.pwa.integrations.govukpay.GovUkPayRequestFailure;
import uk.co.ogauthority.pwa.integrations.govukpay.GovUkPaymentStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PwaPaymentServiceTest {

  private static final int PENNY_AMOUNT = 150;
  private static final String PAYMENT_REFERENCE = "PAYMENT_REFERENCE";
  private static final String PAYMENT_DESCRIPTION = "PAYMENT_DESCRIPTION";

  private static final String METADATA_KEY = "TEST_METADATA";
  private static final String METADATA_VALUE = "test-value";

  private static final String FAKE_RETURN_URL = "/fake/return/url/";

  private static final String CONTEXT_PATH = "applicationContextPath";
  private static final String PWA_URL_BASE = "pwaUrlBase.com/";

  private static final String GOV_PAY_ID = "abc123";

  private final Clock clock = Clock.fixed(
      LocalDateTime.of(2020, 12, 1, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault());

  private UUID paymentUuid;
  private String journeyReturnUrl;

  private InOrder inOrder;

  @Mock
  private GovUkPayCardPaymentClient govUkPayCardPaymentClient;

  @Mock
  private PwaPaymentRequestPersister pwaPaymentRequestPersister;

  @Mock
  private PwaPaymentRequestRepository pwaPaymentRequestRepository;

  private PwaPaymentService pwaPaymentService;

  @BeforeEach
  void setUp() throws Exception {

    paymentUuid = UUID.randomUUID();
    journeyReturnUrl = PWA_URL_BASE + CONTEXT_PATH + PwaPaymentServiceTest.fakeReturnUrlProducer(paymentUuid);

    when(pwaPaymentRequestPersister.createPendingPaymentRequestInNewTransaction(any(), any(), any(), any()))
        .thenReturn(paymentUuid);

    pwaPaymentService = new PwaPaymentService(
        govUkPayCardPaymentClient,
        pwaPaymentRequestPersister,
        pwaPaymentRequestRepository,
        PWA_URL_BASE,
        CONTEXT_PATH
    );

    // contains all mocked services, so that tests can verify the order of interactions.
    inOrder = Mockito.inOrder(
        govUkPayCardPaymentClient,
        pwaPaymentRequestPersister,
        pwaPaymentRequestRepository
    );
  }

  private static String fakeReturnUrlProducer(UUID uuid) {
    return FAKE_RETURN_URL + uuid;
  }

  @Test
  void createCardPayment_whenNewPaymentRequestSucceeds() {
    var newPaymentResult = GovPayNewCardPaymentResultTestUtil.createFrom(
        GOV_PAY_ID,
        GovUkPaymentStatus.CREATED,
        journeyReturnUrl
    );

    ArgumentCaptor<GovPayNewCardPaymentRequest> newPaymentRequestCaptor = ArgumentCaptor.forClass(
        GovPayNewCardPaymentRequest.class);
    when(govUkPayCardPaymentClient.createCardPaymentJourney(any())).thenReturn(newPaymentResult);

    var paymentRequest = new PwaPaymentRequest();
    paymentRequest.setUuid(paymentUuid);
    when(pwaPaymentRequestRepository.findById(paymentUuid)).thenReturn(Optional.of(paymentRequest));

    var createCardPaymentResult = pwaPaymentService.createCardPayment(
        PENNY_AMOUNT,
        PAYMENT_REFERENCE,
        PAYMENT_DESCRIPTION,
        Map.of(METADATA_KEY, METADATA_VALUE),
        PwaPaymentServiceTest::fakeReturnUrlProducer
    );

    inOrder.verify(pwaPaymentRequestPersister, times(1)).createPendingPaymentRequestInNewTransaction(
        PaymentRequestType.CARD_PAYMENT,
        PAYMENT_REFERENCE,
        PAYMENT_DESCRIPTION,
        PENNY_AMOUNT
    );
    inOrder.verify(govUkPayCardPaymentClient, times(1)).createCardPaymentJourney(newPaymentRequestCaptor.capture());
    inOrder.verify(pwaPaymentRequestPersister, times(1)).setPaymentRequestInProgress(paymentUuid, newPaymentResult);
    inOrder.verify(pwaPaymentRequestRepository, times(1)).findById(paymentUuid);
    inOrder.verifyNoMoreInteractions();

    assertThat(newPaymentRequestCaptor.getValue().getAmount()).isEqualTo(PENNY_AMOUNT);
    assertThat(newPaymentRequestCaptor.getValue().getReference()).isEqualTo(PAYMENT_REFERENCE);
    assertThat(newPaymentRequestCaptor.getValue().getDescription()).isEqualTo(PAYMENT_DESCRIPTION);
    assertThat(newPaymentRequestCaptor.getValue().getMetadata()).containsExactly(entry(METADATA_KEY, METADATA_VALUE));
    assertThat(newPaymentRequestCaptor.getValue().getReturnUrl()).isEqualTo(journeyReturnUrl);
    // no point testing this precisely, its going to be whatever we fake it to be while setting up the test.
    assertThat(createCardPaymentResult.getStartExternalJourneyUrl()).isNotEmpty();
    assertThat(createCardPaymentResult.getPwaPaymentRequest()).isEqualTo(paymentRequest);
  }

  @Test
  void createCardPayment_whenNewPaymentCannotBeCreated() {
    var invalidPennyAmount = -10;

    var errorThrown = false;
    try {
      pwaPaymentService.createCardPayment(
          invalidPennyAmount,
          PAYMENT_REFERENCE,
          PAYMENT_DESCRIPTION,
          Map.of(),
          PwaPaymentServiceTest::fakeReturnUrlProducer
      );
    } catch (PwaPaymentsException e) {
      errorThrown = true;
    } catch (Exception e) {
      fail("Expected PwaPaymentException but did not catch it");
    }

    assertThat(errorThrown).isTrue();

    inOrder.verify(pwaPaymentRequestPersister, times(1)).createPendingPaymentRequestInNewTransaction(
        PaymentRequestType.CARD_PAYMENT,
        PAYMENT_REFERENCE,
        PAYMENT_DESCRIPTION,
        invalidPennyAmount
    );
    inOrder.verify(pwaPaymentRequestPersister).setPaymentRequestStatusInNewTransaction(
        eq(paymentUuid),
        eq(PaymentRequestStatus.FAILED_TO_CREATE),
        anyString()
    );
    inOrder.verifyNoMoreInteractions();

  }

  @Test
  void createCardPayment_whenGovPayClientThrowsError() {
    when(govUkPayCardPaymentClient.createCardPaymentJourney(any())).thenThrow(new RuntimeException("some error"));

    var errorThrown = false;
    try {
      pwaPaymentService.createCardPayment(
          PENNY_AMOUNT,
          PAYMENT_REFERENCE,
          PAYMENT_DESCRIPTION,
          Map.of(),
          PwaPaymentServiceTest::fakeReturnUrlProducer
      );
    } catch (PwaPaymentsException e) {
      errorThrown = true;
    } catch (Exception e) {
      fail("Expected PwaPaymentException but did not catch it");
    }

    assertThat(errorThrown).isTrue();

    inOrder.verify(pwaPaymentRequestPersister, times(1)).createPendingPaymentRequestInNewTransaction(
        PaymentRequestType.CARD_PAYMENT,
        PAYMENT_REFERENCE,
        PAYMENT_DESCRIPTION,
        PENNY_AMOUNT
    );
    inOrder.verify(pwaPaymentRequestPersister).setPaymentRequestStatusInNewTransaction(
        eq(paymentUuid),
        eq(PaymentRequestStatus.FAILED_TO_CREATE),
        anyString()
    );
    inOrder.verifyNoMoreInteractions();

  }

  @Test
  void getGovUkPaymentRequestOrError_whenPaymentFound() {
    var paymentRequest = new PwaPaymentRequest();
    when(pwaPaymentRequestRepository.findById(paymentUuid)).thenReturn(Optional.of(paymentRequest));

    assertThat(pwaPaymentService.getGovUkPaymentRequestOrError(paymentUuid)).isEqualTo(paymentRequest);
  }

  @Test
  void getGovUkPaymentRequestOrError_whenPaymentNotFound() {
    assertThrows(PwaPaymentsException.class, () ->

      pwaPaymentService.getGovUkPaymentRequestOrError(paymentUuid));
  }

  @Test
  void refreshPwaPaymentRequestData_whenPaymentFoundLocally_noGovPayId() {
    var paymentRequest = PwaPaymentRequestTestUtil.createFrom(paymentUuid, PaymentRequestStatus.PENDING, null);

    pwaPaymentService.refreshPwaPaymentRequestData(paymentRequest);

    inOrder.verify(pwaPaymentRequestPersister, times(1)).setPaymentRequestStatusData(
        eq(paymentRequest),
        eq(PaymentRequestStatus.COMPLETE_WITHOUT_PAYMENT),
        anyString()
    );
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void refreshPwaPaymentRequestData_whenPaymentFoundLocally_withGovPayId_paymentInProgress() {
    var paymentRequest = PwaPaymentRequestTestUtil.createFrom(
        paymentUuid,
        PaymentRequestStatus.PENDING,
        GOV_PAY_ID
    );

    var govPayJourneyState = GovPayPaymentJourneyStateTestUtil.createFor(GovUkPaymentStatus.STARTED);
    var govPayJourneyData = GovPayPaymentJourneyDataTestUtil.createFrom(
        GOV_PAY_ID,
        govPayJourneyState
    );

    when(govUkPayCardPaymentClient.getCardPaymentJourneyData(any()))
        .thenReturn(govPayJourneyData);

    pwaPaymentService.refreshPwaPaymentRequestData(paymentRequest);

    inOrder.verify(govUkPayCardPaymentClient, times(1)).getCardPaymentJourneyData(GOV_PAY_ID);
    inOrder.verify(pwaPaymentRequestPersister, times(1)).setPaymentRequestGovUkStatusData(
        paymentRequest,
        govPayJourneyState
    );
    inOrder.verify(pwaPaymentRequestPersister, times(1)).setPaymentRequestStatusData(
        eq(paymentRequest),
        eq(PaymentRequestStatus.IN_PROGRESS),
        anyString()
    );

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void refreshPwaPaymentRequestData_whenPaymentFoundLocally_withGovPayId_paymentFinished() {
    var paymentRequest = PwaPaymentRequestTestUtil.createFrom(
        paymentUuid,
        PaymentRequestStatus.PAYMENT_COMPLETE,
        GOV_PAY_ID
    );

    pwaPaymentService.refreshPwaPaymentRequestData(paymentRequest);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void cancelPayment_withGovPayId_alreadyFinished() {

    var paymentRequest = PwaPaymentRequestTestUtil.createFrom(
        paymentUuid,
        PaymentRequestStatus.PAYMENT_COMPLETE,
        GOV_PAY_ID
    );

    pwaPaymentService.cancelPayment(paymentRequest);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void cancelPayment_withGovPayId_inProgress() {

    var paymentRequest = PwaPaymentRequestTestUtil.createFrom(
        paymentUuid,
        PaymentRequestStatus.IN_PROGRESS,
        GOV_PAY_ID
    );

    var govPayJourneyState = GovPayPaymentJourneyStateTestUtil.createFor(GovUkPaymentStatus.CANCELLED);
    var govPayJourneyData = GovPayPaymentJourneyDataTestUtil.createFrom(
        GOV_PAY_ID,
        govPayJourneyState
    );

    when(govUkPayCardPaymentClient.getCardPaymentJourneyData(any()))
        .thenReturn(govPayJourneyData);

    pwaPaymentService.cancelPayment(paymentRequest);

    inOrder.verify(govUkPayCardPaymentClient, times(1)).cancelCardPaymentJourney(GOV_PAY_ID);
    inOrder.verify(govUkPayCardPaymentClient, times(1)).getCardPaymentJourneyData(GOV_PAY_ID);
    inOrder.verify(pwaPaymentRequestPersister, times(1)).setPaymentRequestGovUkStatusData(
        paymentRequest,
        govPayJourneyState
    );
    inOrder.verify(pwaPaymentRequestPersister, times(1)).setPaymentRequestStatusData(
        eq(paymentRequest),
        eq(PaymentRequestStatus.CANCELLED),
        anyString()
    );
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void cancelPayment_withGovPayId_inProgress_govPayCancelRequestErrors() {

    var paymentRequest = PwaPaymentRequestTestUtil.createFrom(
        paymentUuid,
        PaymentRequestStatus.IN_PROGRESS,
        GOV_PAY_ID
    );

    doThrow(new GovUkPayRequestFailure("some error")).when(govUkPayCardPaymentClient).cancelCardPaymentJourney(any());

    var errorThrown = false;
    try {
      pwaPaymentService.cancelPayment(paymentRequest);
    } catch (GovUkPayRequestFailure e) {
      errorThrown = true;
    } catch (Exception e) {
      fail("Expected Gov pay request error");
    }

    assertThat(errorThrown).isTrue();
    inOrder.verify(govUkPayCardPaymentClient, times(1)).cancelCardPaymentJourney(GOV_PAY_ID);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void cancelPayment_withoutGovPayId() {

    var paymentRequest = PwaPaymentRequestTestUtil.createFrom(
        paymentUuid,
        PaymentRequestStatus.PENDING,
        null
    );

    pwaPaymentService.cancelPayment(paymentRequest);

    inOrder.verify(pwaPaymentRequestPersister, times(1)).setPaymentRequestStatusData(
        eq(paymentRequest),
        eq(PaymentRequestStatus.CANCELLED),
        isNull()
    );
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void decodeGovPayStatus_whenJourneyFailed() {
    assertThat(
        pwaPaymentService.decodeGovPayStatus(GovPayPaymentJourneyStateTestUtil.createFor(GovUkPaymentStatus.FAILED)))
        .isEqualTo(PaymentRequestStatus.COMPLETE_WITHOUT_PAYMENT);
  }

  @Test
  void decodeGovPayStatus_whenJourneyIsSuccess() {
    assertThat(
        pwaPaymentService.decodeGovPayStatus(GovPayPaymentJourneyStateTestUtil.createFor(GovUkPaymentStatus.SUCCESS)))
        .isEqualTo(PaymentRequestStatus.PAYMENT_COMPLETE);
  }

  @Test
  void decodeGovPayStatus_whenJourneyIsCancelledByService() {
    assertThat(
        pwaPaymentService.decodeGovPayStatus(GovPayPaymentJourneyStateTestUtil.createFor(GovUkPaymentStatus.CANCELLED)))
        .isEqualTo(PaymentRequestStatus.CANCELLED);
  }

  @Test
  void decodeGovPayStatus_whenJourneyIsOngoing() {
    var journeyInProgressStatuses = Arrays.stream(GovUkPaymentStatus.values())
        .filter(govUkPaymentStatus -> !govUkPaymentStatus.isJourneyFinished())
        .collect(toSet());

    for (GovUkPaymentStatus govUkPaymentStatus : journeyInProgressStatuses) {
      assertThat(pwaPaymentService.decodeGovPayStatus(GovPayPaymentJourneyStateTestUtil.createFor(govUkPaymentStatus)))
          .isEqualTo(PaymentRequestStatus.IN_PROGRESS);
    }
  }
}