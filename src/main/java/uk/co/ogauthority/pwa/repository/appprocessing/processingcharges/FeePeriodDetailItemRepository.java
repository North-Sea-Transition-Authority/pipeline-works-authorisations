package uk.co.ogauthority.pwa.repository.appprocessing.processingcharges;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetailFeeItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Repository
public interface FeePeriodDetailItemRepository extends CrudRepository<FeePeriodDetailFeeItem, Integer> {

  List<FeePeriodDetailFeeItem> findAllByFeePeriodDetailAndFeeItem_PwaApplicationType(FeePeriodDetail feePeriodDetail,
                                                                                     PwaApplicationType applicationType);

}