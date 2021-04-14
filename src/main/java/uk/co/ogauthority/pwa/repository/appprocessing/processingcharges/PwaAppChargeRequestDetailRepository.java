package uk.co.ogauthority.pwa.repository.appprocessing.processingcharges;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

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