package uk.co.ogauthority.pwa.repository.pwaapplications.initial;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.initial.PadData;

@Repository
public interface PadDataRepository extends CrudRepository<PadData, Integer> {

  Optional<PadData> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
