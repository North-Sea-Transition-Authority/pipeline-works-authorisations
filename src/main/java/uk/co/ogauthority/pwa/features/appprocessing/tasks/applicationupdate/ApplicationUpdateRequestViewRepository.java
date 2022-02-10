package uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface ApplicationUpdateRequestViewRepository extends CrudRepository<ApplicationUpdateRequest, Integer> {

  Optional<ApplicationUpdateRequestView> findByPwaApplicationDetailAndStatus(PwaApplicationDetail pwaApplicationDetail,
                                                                             ApplicationUpdateRequestStatus status);

  Optional<ApplicationUpdateRequestView> findByPwaApplicationDetail_pwaApplicationAndStatus(
      PwaApplication pwaApplication,
      ApplicationUpdateRequestStatus status);

  List<ApplicationUpdateRequestView> findAllByPwaApplicationDetail_pwaApplicationAndStatus(
      PwaApplication pwaApplication,
      ApplicationUpdateRequestStatus status);
}