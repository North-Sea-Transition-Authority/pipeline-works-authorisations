package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;

@Repository
public interface FeePeriodDetailItemRepository extends CrudRepository<FeePeriodDetailFeeItem, Integer> {

  List<FeePeriodDetailFeeItem> findAllByFeePeriodDetailAndFeeItem_PwaApplicationTypeAndFeeItem_PwaApplicationFeeType(
      FeePeriodDetail feePeriodDetail,
      PwaApplicationType applicationType,
      PwaApplicationFeeType pwaApplicationFeeType
  );

  List<FeePeriodDetailFeeItem> findAllByFeePeriodDetail(FeePeriodDetail feePeriodDetail);
}