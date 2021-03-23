package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.feeproviders;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.FeePeriodDetailItemRepository;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.ApplicationFeeItemProvider;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.PwaApplicationFeeType;

@Service
class FastTrackFeeItemProvider implements ApplicationFeeItemProvider {

  private static final PwaApplicationFeeType FEE_TYPE = PwaApplicationFeeType.FAST_TRACK;

  private final FeePeriodDetailItemRepository feePeriodDetailItemRepository;

  @Autowired
  FastTrackFeeItemProvider(FeePeriodDetailItemRepository feePeriodDetailItemRepository) {
    this.feePeriodDetailItemRepository = feePeriodDetailItemRepository;
  }

  @Override
  public int getProvisionOrdering() {
    return 20;
  }

  @Override
  public boolean canProvideFeeItems(PwaApplicationDetail pwaApplicationDetail) {
    return pwaApplicationDetail.getSubmittedAsFastTrackFlag();
  }

  @Override
  public List<ApplicationFeeItem> provideFees(FeePeriodDetail feePeriodDetail,
                                              PwaApplicationDetail pwaApplicationDetail) {
    return feePeriodDetailItemRepository.findAllByFeePeriodDetailAndFeeItem_PwaApplicationTypeAndFeeItem_PwaApplicationFeeType(
        feePeriodDetail,
        pwaApplicationDetail.getPwaApplicationType(),
        PwaApplicationFeeType.FAST_TRACK
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
