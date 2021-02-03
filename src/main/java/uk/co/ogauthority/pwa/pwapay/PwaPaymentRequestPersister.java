package uk.co.ogauthority.pwa.pwapay;

import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.govukpay.NewCardPaymentResult;
import uk.co.ogauthority.pwa.govukpay.PaymentJourneyState;

/**
 * Has to be separate class from PWAPaymentService to allow fine grained transaction control.
 */
@Service
public class PwaPaymentRequestPersister {

  private final PwaPaymentRequestRepository pwaPaymentRequestRepository;
  private final Clock clock;

  @Autowired
  public PwaPaymentRequestPersister(PwaPaymentRequestRepository pwaPaymentRequestRepository,
                                    @Qualifier("utcClock") Clock clock) {
    this.pwaPaymentRequestRepository = pwaPaymentRequestRepository;
    this.clock = clock;
  }

  /**
   * Insert a pending request row into the request repo.
   * Having this in a new transaction means we have a record of every attempted payment, even if something goes wrong
   * with the external service.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public UUID createPendingPaymentRequestInNewTransaction(PaymentRequestType paymentService,
                                                          String reference,
                                                          String description,
                                                          Integer amountPennies) {

    var storedRequest = new PwaPaymentRequest();
    storedRequest.setCreatedTimestamp(clock.instant());
    storedRequest.setRequestedService(paymentService);
    storedRequest.setRequestStatus(PaymentRequestStatus.PENDING);
    storedRequest.setRequestStatusTimestamp(clock.instant());
    storedRequest.setReference(reference);
    storedRequest.setDescription(description);
    storedRequest.setAmountPennies(amountPennies);
    pwaPaymentRequestRepository.save(storedRequest);

    return storedRequest.getUuid();
  }


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void setPaymentRequestStatusInNewTransaction(UUID paymentRequestUuid,
                                                      PaymentRequestStatus paymentRequestStatus,
                                                      @Nullable String message) {
    var paymentRequest = getPaymentRequestOrError(paymentRequestUuid);

    setPaymentRequestStatusCommon(paymentRequest, paymentRequestStatus, message);

    pwaPaymentRequestRepository.save(paymentRequest);
  }

  @Transactional
  public void setPaymentRequestStatusData(PwaPaymentRequest paymentRequest,
                                          PaymentRequestStatus paymentRequestStatus,
                                          @Nullable String message) {
    setPaymentRequestStatusCommon(paymentRequest, paymentRequestStatus, message);
    pwaPaymentRequestRepository.save(paymentRequest);
  }

  @Transactional
  public void setPaymentRequestGovUkStatusData(PwaPaymentRequest paymentRequest,
                                               PaymentJourneyState paymentJourneyState) {
    mapPaymentJourneyStateToRequest(paymentRequest, paymentJourneyState);
    pwaPaymentRequestRepository.save(paymentRequest);
  }


  private void setPaymentRequestStatusCommon(PwaPaymentRequest paymentRequest,
                                             PaymentRequestStatus paymentRequestStatus,
                                             @Nullable String message) {

    paymentRequest.setRequestStatus(paymentRequestStatus);
    paymentRequest.setRequestStatusTimestamp(clock.instant());
    paymentRequest.setRequestStatusMessage(message);

  }

  @Transactional
  public void setPaymentRequestInProgress(UUID paymentRequestUuid,
                                          NewCardPaymentResult newCardPaymentResult) {

    var paymentRequest = getPaymentRequestOrError(paymentRequestUuid);
    setPaymentRequestStatusCommon(paymentRequest, PaymentRequestStatus.IN_PROGRESS, null);

    paymentRequest.setGovUkPaymentId(newCardPaymentResult.getPaymentId());
    paymentRequest.setReturnUrl(newCardPaymentResult.getReturnToServiceAfterJourneyCompleteUrl());

    mapPaymentJourneyStateToRequest(paymentRequest, newCardPaymentResult.getPaymentJourneyState());

    pwaPaymentRequestRepository.save(paymentRequest);

  }

  private void mapPaymentJourneyStateToRequest(PwaPaymentRequest paymentRequest,
                                               PaymentJourneyState paymentJourneyState) {

    paymentRequest.setGovUkPaymentStatus(paymentJourneyState.getStatus());
    paymentRequest.setGovUkPaymentStatusMessage(
        String.format("Code: %s %n Message: %s",
            paymentJourneyState.getCode(),
            paymentJourneyState.getMessage()
        )
    );
    paymentRequest.setGovUkPaymentStatusTimestamp(clock.instant());
  }

  private PwaPaymentRequest getPaymentRequestOrError(UUID paymentRequestUuid) {
    return pwaPaymentRequestRepository.findById(paymentRequestUuid)
        .orElseThrow(() -> new PwaPaymentsException(
            String.format("Could not locate payment journey identified by %s", paymentRequestUuid))
        );
  }

}
