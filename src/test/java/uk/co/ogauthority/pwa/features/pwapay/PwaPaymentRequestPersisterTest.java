package uk.co.ogauthority.pwa.features.pwapay;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.integrations.govukpay.GovPayNewCardPaymentResultTestUtil;
import uk.co.ogauthority.pwa.integrations.govukpay.GovPayPaymentJourneyStateTestUtil;
import uk.co.ogauthority.pwa.integrations.govukpay.GovUkPaymentStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PwaPaymentRequestPersisterTest {
  private static final String GOV_PAY_ID = "abc123";

  private static final int PENNY_AMOUNT = 150;
  private static final String PAYMENT_REFERENCE = "PAYMENT_REFERENCE";
  private static final String PAYMENT_DESCRIPTION = "PAYMENT_DESCRIPTION";

  private static final String MESSAGE = "some message";

  private final Clock clock = Clock.fixed(
      LocalDateTime.of(2020, 12, 1, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault());

  @Mock
  private PwaPaymentRequestRepository pwaPaymentRequestRepository;

  private PwaPaymentRequestPersister pwaPaymentRequestPersister;

  private UUID paymentUuid;

  private PwaPaymentRequest paymentRequest;

  @BeforeEach
  void setUp() throws Exception {

    paymentUuid = UUID.randomUUID();
    paymentRequest = PwaPaymentRequestTestUtil.createFrom(paymentUuid, PaymentRequestStatus.PENDING, null);
    when(pwaPaymentRequestRepository.findById(paymentUuid)).thenReturn(Optional.of(paymentRequest));

    // fake auto generation of uuid on repo save.
    when(pwaPaymentRequestRepository.save(any())).thenAnswer(invocation -> {
      var entity = (PwaPaymentRequest) invocation.getArgument(0);
      entity.setUuid(paymentUuid);
      return entity;
    });

    pwaPaymentRequestPersister = new PwaPaymentRequestPersister(pwaPaymentRequestRepository, clock);
  }

  @Test
  void createPendingPaymentRequestInNewTransaction_setExpectedValuesOnNewPaymentRequest() {

    ArgumentCaptor<PwaPaymentRequest> paymentRequestCaptor = ArgumentCaptor.forClass(PwaPaymentRequest.class);

    var newPaymentRequestUuid = pwaPaymentRequestPersister.createPendingPaymentRequestInNewTransaction(
        PaymentRequestType.CARD_PAYMENT,
        PAYMENT_REFERENCE,
        PAYMENT_DESCRIPTION,
        PENNY_AMOUNT
    );

    verify(pwaPaymentRequestRepository, times(1)).save(paymentRequestCaptor.capture());

    assertThat(newPaymentRequestUuid).isEqualTo(paymentUuid);
    assertThat(paymentRequestCaptor.getValue().getUuid()).isEqualTo(newPaymentRequestUuid);
    assertThat(paymentRequestCaptor.getValue().getRequestedService()).isEqualTo(PaymentRequestType.CARD_PAYMENT);
    assertThat(paymentRequestCaptor.getValue().getAmountPennies()).isEqualTo(PENNY_AMOUNT);
    assertThat(paymentRequestCaptor.getValue().getCreatedTimestamp()).isEqualTo(clock.instant());
    assertThat(paymentRequestCaptor.getValue().getReference()).isEqualTo(PAYMENT_REFERENCE);
    assertThat(paymentRequestCaptor.getValue().getDescription()).isEqualTo(PAYMENT_DESCRIPTION);
    assertThat(paymentRequestCaptor.getValue().getReturnUrl()).isNull();
    assertThat(paymentRequestCaptor.getValue().getRequestStatus()).isEqualTo(PaymentRequestStatus.PENDING);
    assertThat(paymentRequestCaptor.getValue().getRequestStatusTimestamp()).isEqualTo(clock.instant());
    assertThat(paymentRequestCaptor.getValue().getRequestStatusMessage()).isNull();
    assertThat(paymentRequestCaptor.getValue().getGovUkPaymentId()).isNull();
    assertThat(paymentRequestCaptor.getValue().getGovUkPaymentStatus()).isNull();
    assertThat(paymentRequestCaptor.getValue().getGovUkPaymentStatusTimestamp()).isNull();
    assertThat(paymentRequestCaptor.getValue().getGovUkPaymentStatusMessage()).isNull();
  }

  @Test
  void setPaymentRequestStatusInNewTransaction_paymentExists() {

    pwaPaymentRequestPersister.setPaymentRequestStatusInNewTransaction(
        paymentUuid,
        PaymentRequestStatus.IN_PROGRESS,
        MESSAGE
    );

    ArgumentCaptor<PwaPaymentRequest> paymentRequestCaptor = ArgumentCaptor.forClass(PwaPaymentRequest.class);
    verify(pwaPaymentRequestRepository, times(1)).save(paymentRequestCaptor.capture());

    assertThat(paymentRequestCaptor.getValue().getRequestStatus()).isEqualTo(PaymentRequestStatus.IN_PROGRESS);
    assertThat(paymentRequestCaptor.getValue().getRequestStatusTimestamp()).isEqualTo(clock.instant());
    assertThat(paymentRequestCaptor.getValue().getRequestStatusMessage()).isEqualTo(MESSAGE);

  }

  @Test
  void setPaymentRequestStatusInNewTransaction_paymentDoesNotExist() {
    when(pwaPaymentRequestRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(PwaPaymentsException.class, () ->

      pwaPaymentRequestPersister.setPaymentRequestStatusInNewTransaction(
          paymentUuid,
          PaymentRequestStatus.IN_PROGRESS,
          MESSAGE
      ));

  }

  @Test
  void setPaymentRequestStatusData_setsStatusColumnsAsExpected() {
    pwaPaymentRequestPersister.setPaymentRequestStatusData(
        paymentRequest,
        PaymentRequestStatus.IN_PROGRESS,
        MESSAGE
    );

    verify(pwaPaymentRequestRepository, times(1)).save(paymentRequest);

    assertThat(paymentRequest.getRequestStatus()).isEqualTo(PaymentRequestStatus.IN_PROGRESS);
    assertThat(paymentRequest.getRequestStatusTimestamp()).isEqualTo(clock.instant());
    assertThat(paymentRequest.getRequestStatusMessage()).isEqualTo(MESSAGE);

  }

  @Test
  void setPaymentRequestGovUkStatusData_whenNoCode_andNoMessage() {
    paymentRequest.setGovUkPaymentStatus(GovUkPaymentStatus.CREATED);
    paymentRequest.setGovUkPaymentStatusTimestamp(Instant.now());
    paymentRequest.setGovUkPaymentStatusMessage("some old message");

    pwaPaymentRequestPersister.setPaymentRequestGovUkStatusData(
        paymentRequest,
        GovPayPaymentJourneyStateTestUtil.createFor(GovUkPaymentStatus.STARTED)
    );

    assertThat(paymentRequest.getGovUkPaymentStatus()).isEqualTo(GovUkPaymentStatus.STARTED);
    assertThat(paymentRequest.getGovUkPaymentStatusTimestamp()).isEqualTo(clock.instant());
    assertThat(paymentRequest.getGovUkPaymentStatusMessage()).isNull();
  }

  @Test
  void setPaymentRequestGovUkStatusData_whenCode_andNoMessage() {
    paymentRequest.setGovUkPaymentStatus(GovUkPaymentStatus.CREATED);
    paymentRequest.setGovUkPaymentStatusTimestamp(Instant.now());
    paymentRequest.setGovUkPaymentStatusMessage(null);

    var code = "P987";
    var govPayPaymentJourney = GovPayPaymentJourneyStateTestUtil.createFailedJourneyState(
        null,
        code
    );

    pwaPaymentRequestPersister.setPaymentRequestGovUkStatusData(
        paymentRequest,
        govPayPaymentJourney
    );

    assertThat(paymentRequest.getGovUkPaymentStatus()).isEqualTo(GovUkPaymentStatus.FAILED);
    assertThat(paymentRequest.getGovUkPaymentStatusTimestamp()).isEqualTo(clock.instant());
    assertThat(paymentRequest.getGovUkPaymentStatusMessage()).contains(code);
  }

  @Test
  void setPaymentRequestGovUkStatusData_whenNoCode_andSomeMessage() {
    paymentRequest.setGovUkPaymentStatus(GovUkPaymentStatus.CREATED);
    paymentRequest.setGovUkPaymentStatusTimestamp(Instant.now());
    paymentRequest.setGovUkPaymentStatusMessage(null);

    var govPayPaymentJourney = GovPayPaymentJourneyStateTestUtil.createFailedJourneyState(
        MESSAGE,
        null
    );

    pwaPaymentRequestPersister.setPaymentRequestGovUkStatusData(
        paymentRequest,
        govPayPaymentJourney
    );

    assertThat(paymentRequest.getGovUkPaymentStatus()).isEqualTo(GovUkPaymentStatus.FAILED);
    assertThat(paymentRequest.getGovUkPaymentStatusTimestamp()).isEqualTo(clock.instant());
    assertThat(paymentRequest.getGovUkPaymentStatusMessage()).contains(MESSAGE);
  }

  @Test
  void setPaymentRequestInProgress_whenPaymentRequestFound() {
    var govUkPayNewCardPaymentResult = GovPayNewCardPaymentResultTestUtil.createFrom(GOV_PAY_ID, GovUkPaymentStatus.CREATED, "someUrl" );

    pwaPaymentRequestPersister.setPaymentRequestInProgress(paymentUuid, govUkPayNewCardPaymentResult);

    ArgumentCaptor<PwaPaymentRequest> paymentRequestCaptor = ArgumentCaptor.forClass(PwaPaymentRequest.class);
    verify(pwaPaymentRequestRepository, times(1)).save(paymentRequestCaptor.capture());

    var capturedPaymentRequest = paymentRequestCaptor.getValue();

    assertThat(capturedPaymentRequest.getReturnUrl()).isEqualTo(govUkPayNewCardPaymentResult.getReturnToServiceAfterJourneyCompleteUrl());

    assertThat(capturedPaymentRequest.getRequestStatus()).isEqualTo(PaymentRequestStatus.IN_PROGRESS);
    assertThat(capturedPaymentRequest.getRequestStatusTimestamp()).isEqualTo(clock.instant());
    assertThat(capturedPaymentRequest.getRequestStatusMessage()).isNull();

    assertThat(capturedPaymentRequest.getGovUkPaymentStatus()).isEqualTo(GovUkPaymentStatus.CREATED);
    assertThat(capturedPaymentRequest.getGovUkPaymentStatusTimestamp()).isEqualTo(clock.instant());
    assertThat(capturedPaymentRequest.getGovUkPaymentStatusMessage()).isNull();
  }

  @Test
  void setPaymentRequestInProgress_whenPaymentRequestNotFound() {
    var govUkPayNewCardPaymentResult = GovPayNewCardPaymentResultTestUtil.createFrom(
          GOV_PAY_ID,
          GovUkPaymentStatus.CREATED,
          "someUrl"
      );
    assertThrows(PwaPaymentsException.class, () ->

      pwaPaymentRequestPersister.setPaymentRequestInProgress(UUID.randomUUID(), govUkPayNewCardPaymentResult));

  }
}