package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.feeproviders;



import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeeItem;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetailFeeItem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.FeePeriodDetailItemRepository;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class FastTrackFeeItemProviderTest {

  private static final String FEE_DESC = "FEE_1";
  private static final int FEE_AMOUNT = 100;

  private static final PwaApplicationFeeType FEE_TYPE = PwaApplicationFeeType.FAST_TRACK;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.DECOMMISSIONING;

  @Mock
  private FeePeriodDetailItemRepository feePeriodDetailItemRepository;

  private FastTrackFeeItemProvider feeItemProvider;

  private PwaApplicationDetail pwaApplicationDetail;

  private FeePeriodDetail feePeriodDetail;
  private FeePeriodDetailFeeItem feePeriodDetailFeeItem;
  private FeeItem feeItem;

  @Before
  public void setUp() throws Exception {

    feeItemProvider = new FastTrackFeeItemProvider(feePeriodDetailItemRepository);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE);

    feePeriodDetail = new FeePeriodDetail();

    feeItem = new FeeItem();
    feeItem.setPwaApplicationFeeType(FEE_TYPE);
    feeItem.setDisplayDescription(FEE_DESC);
    feeItem.setPwaApplicationType(APP_TYPE);

    feePeriodDetailFeeItem = new FeePeriodDetailFeeItem();
    feePeriodDetailFeeItem.setPennyAmount(FEE_AMOUNT);
    feePeriodDetailFeeItem.setFeeItem(feeItem);

    when(feePeriodDetailItemRepository.findAllByFeePeriodDetailAndFeeItem_PwaApplicationTypeAndFeeItem_PwaApplicationFeeType(
        any(), any(), any()
    )).thenReturn(List.of(feePeriodDetailFeeItem));
  }

  @Test
  public void getProvisionOrdering_valueTest() {
    assertThat(feeItemProvider.getProvisionOrdering()).isEqualTo(20);
  }

  @Test
  public void canProvideFeeItems_whenSubmittedAsFastTrack() {
    pwaApplicationDetail.setSubmittedAsFastTrackFlag(true);
    assertThat(feeItemProvider.canProvideFeeItems(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canProvideFeeItems_whenNotSubmittedAsFastTrack() {
    pwaApplicationDetail.setSubmittedAsFastTrackFlag(false);
    assertThat(feeItemProvider.canProvideFeeItems(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void provideFees() {
    var feeItems = feeItemProvider.provideFees(feePeriodDetail, pwaApplicationDetail);
    assertThat(feeItems).hasOnlyOneElementSatisfying(
        applicationFeeItem -> assertThat(applicationFeeItem).isEqualTo(ApplicationFeeItemTestUtil.createAppFeeItem(FEE_DESC, FEE_AMOUNT))
    );

    verify(feePeriodDetailItemRepository, times(1)).findAllByFeePeriodDetailAndFeeItem_PwaApplicationTypeAndFeeItem_PwaApplicationFeeType(
        feePeriodDetail, APP_TYPE, FEE_TYPE
    );

  }

  @Test
  public void getApplicationFeeType() {
    assertThat(feeItemProvider.getApplicationFeeType()).isEqualTo(FEE_TYPE);
  }
}