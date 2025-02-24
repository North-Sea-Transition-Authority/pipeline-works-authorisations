package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeeItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetail;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailFeeItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailItemRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class DefaultFeeItemProviderTest {

  private static final String FEE_DESC = "FEE_1";
  private static final int FEE_AMOUNT = 100;

  private static final PwaApplicationFeeType FEE_TYPE = PwaApplicationFeeType.DEFAULT;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.DECOMMISSIONING;

  @Mock
  private FeePeriodDetailItemRepository feePeriodDetailItemRepository;

  private DefaultFeeItemProvider feeItemProvider;

  private PwaApplicationDetail pwaApplicationDetail;

  private FeePeriodDetail feePeriodDetail;
  private FeePeriodDetailFeeItem feePeriodDetailFeeItem;
  private FeeItem feeItem;

  @BeforeEach
  void setUp() {

    feeItemProvider = new DefaultFeeItemProvider(feePeriodDetailItemRepository);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE);

    feePeriodDetail = new FeePeriodDetail();

    feeItem = new FeeItem();
    feeItem.setPwaApplicationFeeType(FEE_TYPE);
    feeItem.setDisplayDescription(FEE_DESC);
    feeItem.setPwaApplicationType(APP_TYPE);

    feePeriodDetailFeeItem = new FeePeriodDetailFeeItem();
    feePeriodDetailFeeItem.setPennyAmount(FEE_AMOUNT);
    feePeriodDetailFeeItem.setFeeItem(feeItem);

  }

  @Test
  void getProvisionOrdering_valueTest() {
    assertThat(feeItemProvider.getProvisionOrdering()).isEqualTo(10);
  }

  @Test
  void canProvideFeeItems_valueTest() {
    assertThat(feeItemProvider.canProvideFeeItems(pwaApplicationDetail)).isTrue();
  }

  @Test
  void provideFees() {
    when(feePeriodDetailItemRepository.findAllByFeePeriodDetailAndFeeItem_PwaApplicationTypeAndFeeItem_PwaApplicationFeeType(
        any(), any(), any()
    )).thenReturn(List.of(feePeriodDetailFeeItem));

    var feeItems = feeItemProvider.provideFees(feePeriodDetail, pwaApplicationDetail);
    assertThat(feeItems).hasOnlyOneElementSatisfying(
        applicationFeeItem -> assertThat(applicationFeeItem).isEqualTo(ApplicationFeeItemTestUtil.createAppFeeItem(FEE_DESC, FEE_AMOUNT))
    );

    verify(feePeriodDetailItemRepository, times(1)).findAllByFeePeriodDetailAndFeeItem_PwaApplicationTypeAndFeeItem_PwaApplicationFeeType(
        feePeriodDetail, APP_TYPE, FEE_TYPE
    );

  }

  @Test
  void getApplicationFeeType() {
    assertThat(feeItemProvider.getApplicationFeeType()).isEqualTo(FEE_TYPE);
  }
}