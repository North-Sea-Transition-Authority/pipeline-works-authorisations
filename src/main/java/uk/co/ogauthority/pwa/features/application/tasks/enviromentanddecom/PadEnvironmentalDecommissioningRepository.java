package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadEnvironmentalDecommissioningRepository extends CrudRepository<PadEnvironmentalDecommissioning, Integer> {

  Optional<PadEnvironmentalDecommissioning> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
