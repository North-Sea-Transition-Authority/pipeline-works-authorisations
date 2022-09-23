package uk.co.ogauthority.pwa.features.feemanagement.display.internal;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;

public interface FeePeriodItemViewRepository extends CrudRepository<DisplayableFeeItemDetail, Integer> {
  List<DisplayableFeeItemDetail> findAllByFeePeriodId(Integer feePeriodId);

  List<DisplayableFeeItemDetail> findAllByFeePeriodIdAndApplicationFeeType(Integer feePeriodId, PwaApplicationFeeType applicationFeeType);
}
