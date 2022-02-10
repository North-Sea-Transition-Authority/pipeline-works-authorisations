package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.ApplicationFeeItemProvider;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetail;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailItemRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
class DefaultFeeItemProvider implements ApplicationFeeItemProvider {
  private static final PwaApplicationFeeType FEE_TYPE = PwaApplicationFeeType.DEFAULT;

  private final FeePeriodDetailItemRepository feePeriodDetailItemRepository;

  @Autowired
  DefaultFeeItemProvider(FeePeriodDetailItemRepository feePeriodDetailItemRepository) {
    this.feePeriodDetailItemRepository = feePeriodDetailItemRepository;
  }

  @Override
  public int getProvisionOrdering() {
    return 10;
  }

  @Override
  public boolean canProvideFeeItems(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }

  @Override
  public List<ApplicationFeeItem> provideFees(FeePeriodDetail feePeriodDetail,
                                              PwaApplicationDetail pwaApplicationDetail) {
    return feePeriodDetailItemRepository.findAllByFeePeriodDetailAndFeeItem_PwaApplicationTypeAndFeeItem_PwaApplicationFeeType(
        feePeriodDetail,
        pwaApplicationDetail.getPwaApplicationType(),
        FEE_TYPE
    )
        .stream()
        .map(feePeriodDetailFeeItem -> new ApplicationFeeItem(
            feePeriodDetailFeeItem.getFeeItem().getDisplayDescription(),
            feePeriodDetailFeeItem.getPennyAmount())
        ).collect(Collectors.toUnmodifiableList());
  }

  @Override
  public PwaApplicationFeeType getApplicationFeeType() {
    return FEE_TYPE;
  }
}
