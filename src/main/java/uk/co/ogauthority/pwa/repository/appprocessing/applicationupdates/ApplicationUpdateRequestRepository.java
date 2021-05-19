package uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.applicationupdates.ApplicationUpdateRequestStatus;

@Repository
public interface ApplicationUpdateRequestRepository extends CrudRepository<ApplicationUpdateRequest, Integer> {

  boolean existsByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<ApplicationUpdateRequest> findByPwaApplicationDetail_pwaApplicationAndStatus(PwaApplication pwaApplication,
                                                                                        ApplicationUpdateRequestStatus status);

  List<ApplicationUpdateRequest> findAllByPwaApplicationDetailIn(List<PwaApplicationDetail> pwaApplicationDetails);


}