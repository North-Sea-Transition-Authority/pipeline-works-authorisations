package uk.co.ogauthority.pwa.repository.pwaapplications;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Repository
public interface PwaApplicationDetailRepository extends CrudRepository<PwaApplicationDetail, Integer> {

  /**
   * Find current version of PWA application detail if status matches passed-in status.
   */
  Optional<PwaApplicationDetail> findByPwaApplicationIdAndStatusAndTipFlagIsTrue(Integer pwaApplicationId,
                                                                                 PwaApplicationStatus status);

}
