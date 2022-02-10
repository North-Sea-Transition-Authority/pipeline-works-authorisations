package uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface ApplicationUpdateRequestRepository extends CrudRepository<ApplicationUpdateRequest, Integer> {

  boolean existsByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<ApplicationUpdateRequest> findByPwaApplicationDetail_pwaApplicationAndStatus(PwaApplication pwaApplication,
                                                                                        ApplicationUpdateRequestStatus status);

  List<ApplicationUpdateRequest> findAllByPwaApplicationDetailIn(List<PwaApplicationDetail> pwaApplicationDetails);


}