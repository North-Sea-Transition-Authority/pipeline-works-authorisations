package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;

@Repository
public interface FeeItemRepository extends CrudRepository<FeeItem, Integer> {
  Optional<FeeItem> findByPwaApplicationTypeAndPwaApplicationFeeType(PwaApplicationType pwaApplicationType,
                                                                     PwaApplicationFeeType pwaApplicationFeeType);
}