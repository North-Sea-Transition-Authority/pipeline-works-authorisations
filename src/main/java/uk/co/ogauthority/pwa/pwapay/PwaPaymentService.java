package uk.co.ogauthority.pwa.pwapay;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.govukpay.GovUkPayCardPaymentClient;
import uk.co.ogauthority.pwa.govukpay.NewCardPaymentRequest;
import uk.co.ogauthority.pwa.govukpay.PaymentJourneyState;

@Service
public class PwaPaymentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaPaymentService.class);

  public static final String GOVUK_PAYMENT_SUCCESS_STATUS = "success";
  public static final String GOVUK_PAYMENT_CANCELLED_STATUS = "cancelled"; //TODO PWA -1113 is this correct?

  private final GovUkPayCardPaymentClient govUkPayCardPaymentClient;
  private final PwaPaymentRequestRepository pwaPaymentRequestRepository;
  private final PwaPaymentRequestPersister pwaPaymentRequestPersister;
  private final String pwaUrlBase;
  private final String contextPath;

  @Autowired
  public PwaPaymentService(GovUkPayCardPaymentClient govUkPayCardPaymentClient,

                           PwaPaymentRequestPersister pwaPaymentRequestPersister,
                           @Qualifier("utcClock") Clock clock,
                           PwaPaymentRequestRepository pwaPaymentRequestRepository,
                           @Value("${pwa.url.base}") String pwaUrlBase,
                           @Value("${context-path}") String contextPath
  ) {
    this.govUkPayCardPaymentClient = govUkPayCardPaymentClient;
    this.pwaPaymentRequestPersister = pwaPaymentRequestPersister;
    this.pwaPaymentRequestRepository = pwaPaymentRequestRepository;
    this.pwaUrlBase = pwaUrlBase;
    this.contextPath = contextPath;
  }

  /**
   * Create and stores record of new card payment journey.
   * Returns url that allows for completion of the new payment journey on through external app.
   */
  @Transactional
  public String createCardPayment(Integer pennyAmount,
                                  String reference,
                                  String description,
                                  Function<UUID, String> returnUrlSupplier) {

    // new transaction launched here so we can save the request attempt even if later code fails.
    var newPwaPaymentRequestUuid = pwaPaymentRequestPersister.createPendingPaymentRequestInNewTransaction(
        PaymentRequestType.CARD_PAYMENT,
        reference,
        description,
        pennyAmount
    );

    // make sure return url can be used externally
    var returnUrl = pwaUrlBase + contextPath + returnUrlSupplier.apply(newPwaPaymentRequestUuid);

    try {
      var paymentRequest = new NewCardPaymentRequest(
          pennyAmount,
          reference,
          description,
          returnUrl
      );

      var result = govUkPayCardPaymentClient.createCardPaymentJourney(paymentRequest);
      pwaPaymentRequestPersister.setPaymentRequestInProgress(newPwaPaymentRequestUuid, result);
      return result.getStartExternalPaymentJourneyUrl();
    } catch (Exception e) {

      pwaPaymentRequestPersister.setPaymentRequestStatusInNewTransaction(
          newPwaPaymentRequestUuid,
          PaymentRequestStatus.FAILED_TO_CREATE,
          e.getMessage()
      );

      throw new PwaPaymentsException(
          "Failed to create gov uk card payment for uuid:" + newPwaPaymentRequestUuid,
          e
      );
    }
  }

  @Transactional(readOnly = true)
  public Page<PwaPaymentRequest> getPwaPaymentRequests(int page) {
    var pageRequest = PageRequest.of(page, 20, Sort.by(PwaPaymentRequest_.CREATED_TIMESTAMP).descending());
    return pwaPaymentRequestRepository.findAll(pageRequest);
  }

  @Transactional(readOnly = true)
  public PwaPaymentRequest getGovUkPaymentRequestOrError(UUID uuid) {
    return pwaPaymentRequestRepository.findById(uuid)
        .orElseThrow(() -> new PwaPaymentsException(
            String.format("Could not locate payment journey identified by %s", uuid))
        );
  }

  @Transactional
  public void refreshPwaPaymentRequestData(PwaPaymentRequest pwaPaymentRequest) {
    refreshFromGovUkPayOrElseFallback(pwaPaymentRequest,
        paymentRequest -> {
          LOGGER.debug("Refresh of in progress request without govUkPaymentId. Set as complete without payment");
          pwaPaymentRequestPersister.setPaymentRequestStatusData(
              pwaPaymentRequest,
              PaymentRequestStatus.COMPLETE_WITHOUT_PAYMENT,
              "Could not contact gov uk for payment request - marked as complete without payment"
          );
        },
        paymentRequest -> LOGGER.debug("Refresh of finished payment journey attempted. Nothing done.")
    );
  }

  @Transactional
  public void cancelPayment(PwaPaymentRequest pwaPaymentRequest) {

    doWhenInProgressAndExistsInGovUkPay(
        pwaPaymentRequest,
        paymentRequest -> govUkPayCardPaymentClient.cancelCardPaymentJourney(pwaPaymentRequest.getGovUkPaymentId())
    );

    refreshFromGovUkPayOrElseFallback(
        pwaPaymentRequest,
        paymentRequest -> {
          LOGGER.debug("Cancelled in progress request without govUkPaymentId.");
          pwaPaymentRequestPersister.setPaymentRequestStatusData(
              pwaPaymentRequest,
              PaymentRequestStatus.CANCELLED,
              null
          );
        },
        paymentRequest -> LOGGER.debug("Cancellation of finished payment request attempted. Nothing done")
    );

  }

  // TODO PWA-1113 needs independent test?
  private PaymentRequestStatus decodeGovPayStatus(PaymentJourneyState paymentJourneyState) {
    if (paymentJourneyState.isFinished()) {
      if (GOVUK_PAYMENT_SUCCESS_STATUS.equals(paymentJourneyState.getStatus())) {
        return PaymentRequestStatus.PAYMENT_COMPLETE;
      } else if (GOVUK_PAYMENT_CANCELLED_STATUS.equals(paymentJourneyState.getStatus())) {
        return PaymentRequestStatus.CANCELLED;
      } else {
        return PaymentRequestStatus.COMPLETE_WITHOUT_PAYMENT;
      }
    } else {
      return PaymentRequestStatus.IN_PROGRESS;
    }

  }


  private boolean isInProgressAndGovUkPaymentExists(PwaPaymentRequest paymentRequest) {
    return paymentRequest.isInJourneyState(PaymentRequestStatus.JourneyState.IN_PROGRESS)
        && paymentRequest.hasGovUkPaymentId();
  }

  private void doWhenInProgressAndExistsInGovUkPay(PwaPaymentRequest paymentRequest,
                                                   Consumer<PwaPaymentRequest> paymentRequestConsumer) {
    if (isInProgressAndGovUkPaymentExists(paymentRequest)) {
      paymentRequestConsumer.accept(paymentRequest);
    }

  }

  /**
   *  Refresh local request data from govuk pay if possible.
   */
  private void refreshFromGovUkPayOrElseFallback(PwaPaymentRequest paymentRequest,
                                                 Consumer<PwaPaymentRequest> inProgressFallbackConsumer,
                                                 Consumer<PwaPaymentRequest> finishedFallbackConsumer) {
    if (isInProgressAndGovUkPaymentExists(paymentRequest)) {
      var paymentJourneyData = govUkPayCardPaymentClient.getCardPaymentJourneyData(paymentRequest.getGovUkPaymentId());
      var paymentJourneyState = paymentJourneyData.getPaymentJourneyState();
      var requestStatus = decodeGovPayStatus(paymentJourneyState);
      pwaPaymentRequestPersister.setPaymentRequestGovUkStatusData(paymentRequest, paymentJourneyState);
      pwaPaymentRequestPersister.setPaymentRequestStatusData(paymentRequest, requestStatus, "");

    } else if (paymentRequest.isInJourneyState(PaymentRequestStatus.JourneyState.IN_PROGRESS)) {
      inProgressFallbackConsumer.accept(paymentRequest);
    } else {
      finishedFallbackConsumer.accept(paymentRequest);
    }
  }
}
