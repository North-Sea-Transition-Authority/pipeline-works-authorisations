package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadLocationDetailsRepository extends CrudRepository<PadLocationDetails, Integer> {

  Optional<PadLocationDetails> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
