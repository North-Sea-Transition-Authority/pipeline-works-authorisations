package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadFacilityRepository extends CrudRepository<PadFacility, Integer> {

  List<PadFacility> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);


}
