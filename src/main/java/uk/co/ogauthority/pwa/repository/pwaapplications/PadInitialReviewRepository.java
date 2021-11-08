package uk.co.ogauthority.pwa.repository.pwaapplications;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PadInitialReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadInitialReviewRepository extends CrudRepository<PadInitialReview, Integer> {

  List<PadInitialReview> findAllByPwaApplicationDetailIn(List<PwaApplicationDetail> pwaApplicationDetails);

  List<PadInitialReview> findByPwaApplicationDetail_pwaApplicationAndApprovalRevokedTimestampIsNull(
      PwaApplication pwaApplication);

  Optional<PadInitialReview> findFirstByPwaApplicationDetailOrderByInitialReviewApprovedTimestampDesc(
      PwaApplicationDetail pwaApplicationDetail);

}