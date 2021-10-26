package uk.co.ogauthority.pwa.features.application.tasks.designopconditions;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


public interface PadDesignOpConditionsRepository extends CrudRepository<PadDesignOpConditions, Integer> {

  Optional<PadDesignOpConditions> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);
}
