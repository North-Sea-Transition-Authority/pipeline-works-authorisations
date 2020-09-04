package uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates;


import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface ApplicationUpdateRequestRepository extends CrudRepository<ApplicationUpdateRequest, Integer> {

  boolean existsByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}