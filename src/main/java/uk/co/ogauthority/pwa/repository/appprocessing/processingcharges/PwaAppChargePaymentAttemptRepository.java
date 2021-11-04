package uk.co.ogauthority.pwa.repository.appprocessing.processingcharges;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.pwapay.PaymentRequestStatus;
import uk.co.ogauthority.pwa.features.pwapay.PwaPaymentRequest;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargePaymentAttempt;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequest;

@Repository
public interface PwaAppChargePaymentAttemptRepository extends CrudRepository<PwaAppChargePaymentAttempt, Integer> {

  List<PwaAppChargePaymentAttempt> findAllByPwaAppChargeRequestAndActiveFlagIsTrue(PwaAppChargeRequest pwaAppChargeRequest);

  @EntityGraph(attributePaths = {"pwaAppChargeRequest", "pwaPaymentRequest"})
  List<PwaAppChargePaymentAttempt> findAllByActiveFlagIsTrueAndPwaPaymentRequest_RequestStatusAndCreatedTimestampIsBefore(
      PaymentRequestStatus paymentRequestStatus,
      Instant attemptCreatedTimeStampIsBefore
  );

  List<PwaAppChargePaymentAttempt> findAllByPwaAppChargeRequestAndPwaPaymentRequest_RequestStatus(
      PwaAppChargeRequest pwaAppChargeRequest,
      PaymentRequestStatus paymentRequestStatus
  );

  Optional<PwaAppChargePaymentAttempt> findByPwaPaymentRequest(PwaPaymentRequest paymentRequest);

}