package uk.co.ogauthority.pwa.repository.devuk;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadFacilityRepository extends CrudRepository<PadFacility, Integer> {

  List<PadFacility> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
