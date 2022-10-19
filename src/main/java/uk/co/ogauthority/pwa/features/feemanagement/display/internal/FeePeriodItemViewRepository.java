package uk.co.ogauthority.pwa.features.feemanagement.display.internal;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface FeePeriodItemViewRepository extends CrudRepository<DisplayableFeeItemDetail, Integer> {

  List<DisplayableFeeItemDetail> findAllByFeePeriodId(Integer feePeriodId);

}
