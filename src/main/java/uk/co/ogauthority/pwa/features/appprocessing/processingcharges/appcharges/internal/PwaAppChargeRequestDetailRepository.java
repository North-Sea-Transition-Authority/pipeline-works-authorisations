package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.PwaAppChargeRequestStatus;

@Repository
public interface PwaAppChargeRequestDetailRepository extends CrudRepository<PwaAppChargeRequestDetail, Integer> {

  Optional<PwaAppChargeRequestDetail> findByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
      PwaApplication pwaApplication,
      PwaAppChargeRequestStatus pwaAppChargeRequestStatus
  );

  long countByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
      PwaApplication pwaApplication,
      PwaAppChargeRequestStatus pwaAppChargeRequestStatus
  );

  List<PwaAppChargeRequestDetail> findByPwaAppChargeRequest_PwaApplicationAndTipFlagIsTrue(PwaApplication pwaApplication);

}