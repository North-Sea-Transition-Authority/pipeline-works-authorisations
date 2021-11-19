package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders.ApplicationFeeItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders.ApplicationFeeItemTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetail;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFeeServiceTest {
  private static final String FEE_DESC = "FEE_1";
  private static final int FEE_AMOUNT = 100;
  private static final String APP_REF = "APP_REF";

  private static final String FAST_TRACK_STRING = "Fast-track";

  @Mock
  private FeePeriodDetailRepository feePeriodDetailRepository;

  @Mock
  private ApplicationFeeItemProvider feeItemProvider;

  private Clock clock = Clock.fixed(
      LocalDateTime.of(2020, 12, 1, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault());

  private ApplicationFeeService applicationFeeService;

  private PwaApplicationDetail pwaApplicationDetail;

  private FeePeriodDetail feePeriodDetail;
  private ApplicationFeeItem applicationFeeItem;

  @Before
  public void setUp() throws Exception {


    feePeriodDetail = FeePeriodTestUtil.createDefaultFeePeriodDetail();
    applicationFeeItem = ApplicationFeeItemTestUtil.createAppFeeItem(FEE_DESC, FEE_AMOUNT);
    when(feeItemProvider.provideFees(any(), any())).thenReturn(List.of(applicationFeeItem));
    when(feeItemProvider.canProvideFeeItems(any())).thenReturn(true);
    when(feeItemProvider.getProvisionOrdering()).thenReturn(1);

    when(feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodStartTimestampIsBeforeAndPeriodEndTimestampIsAfter(any(),
        any()))
        .thenReturn(Optional.of(feePeriodDetail));
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setAppReference(APP_REF);

    applicationFeeService = new ApplicationFeeService(
        feePeriodDetailRepository,
        List.of(feeItemProvider),
        clock
    );
  }

  @Test
  public void getApplicationFeeReport_currentFeePeriodExists_providerCanProvideFeeItems() {

    when(feeItemProvider.getApplicationFeeType()).thenReturn(PwaApplicationFeeType.DEFAULT);

    var report = applicationFeeService.getApplicationFeeReport(pwaApplicationDetail);

    ArgumentCaptor<Instant> maxPeriodInstantCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(feePeriodDetailRepository, times(1))
        .findByTipFlagIsTrueAndPeriodStartTimestampIsBeforeAndPeriodEndTimestampIsAfter(eq(clock.instant()),
            maxPeriodInstantCaptor.capture());

    // confirm that the default period end date provided when is suitably far away
    var maxDateMinimum = LocalDate.of(4000, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    assertThat(maxPeriodInstantCaptor.getValue()).isAfterOrEqualTo(maxDateMinimum);

    assertThat(report.getPaymentItems()).containsExactly(ApplicationFeeItemTestUtil.createAppFeeItem(FEE_DESC, FEE_AMOUNT));
    assertThat(report.getPwaApplication()).isEqualTo(pwaApplicationDetail.getPwaApplication());
    assertThat(report.getSummary()).containsOnlyOnce(APP_REF);
    assertThat(report.getSummary()).doesNotContain(FAST_TRACK_STRING);
    assertThat(report.getTotalPennies()).isEqualTo(FEE_AMOUNT);

    verify(feeItemProvider, times(1)).canProvideFeeItems(pwaApplicationDetail);
    verify(feeItemProvider, times(1)).provideFees(feePeriodDetail, pwaApplicationDetail);

  }

  @Test
  public void getApplicationFeeReport_currentFeePeriodExists_providerCannotProvideFeeItems() {

    when(feeItemProvider.getApplicationFeeType()).thenReturn(PwaApplicationFeeType.DEFAULT);
    when(feeItemProvider.canProvideFeeItems(any())).thenReturn(false);

    var report = applicationFeeService.getApplicationFeeReport(pwaApplicationDetail);

    assertThat(report.getPaymentItems()).isEmpty();
    assertThat(report.getPwaApplication()).isEqualTo(pwaApplicationDetail.getPwaApplication());
    assertThat(report.getSummary()).containsOnlyOnce(APP_REF);
    assertThat(report.getTotalPennies()).isEqualTo(0);

  }

  @Test
  public void getApplicationFeeReport_currentFeePeriodExists_fastTrack() {
    when(feeItemProvider.getApplicationFeeType()).thenReturn(PwaApplicationFeeType.FAST_TRACK);

    var report = applicationFeeService.getApplicationFeeReport(pwaApplicationDetail);

    assertThat(report.getPaymentItems())
        .hasOnlyOneElementSatisfying(applicationFeeItem -> {
          assertThat(applicationFeeItem.getPennyAmount()).isEqualTo(FEE_AMOUNT);
          assertThat(applicationFeeItem.getDescription()).isEqualTo(FEE_DESC);
        });

    assertThat(report.getPwaApplication()).isEqualTo(pwaApplicationDetail.getPwaApplication());
    assertThat(report.getSummary()).containsOnlyOnce(APP_REF);
    assertThat(report.getSummary()).containsOnlyOnce(FAST_TRACK_STRING);
    assertThat(report.getTotalPennies()).isEqualTo(FEE_AMOUNT);

  }

  @Test
  public void getApplicationFeeReport_combinesAppFeesFromMultipleProviders() {

    when(feeItemProvider.getApplicationFeeType()).thenReturn(PwaApplicationFeeType.DEFAULT);

    var feeItemProvider2 = mock(ApplicationFeeItemProvider.class);
    when(feeItemProvider2.canProvideFeeItems(pwaApplicationDetail)).thenReturn(true);
    when(feeItemProvider2.getApplicationFeeType()).thenReturn(PwaApplicationFeeType.FAST_TRACK);
    when(feeItemProvider2.getProvisionOrdering()).thenReturn(2);

    var appFeeItem2 = ApplicationFeeItemTestUtil.createAppFeeItem("Fee item 2", 200);
    when(feeItemProvider2.provideFees(any(), any())).thenReturn(List.of(appFeeItem2));

    applicationFeeService = new ApplicationFeeService(
        feePeriodDetailRepository,
        List.of(feeItemProvider, feeItemProvider2),
        clock
    );

    var report = applicationFeeService.getApplicationFeeReport(pwaApplicationDetail);

    assertThat(report.getPaymentItems()).hasSize(2)
        .containsExactly(applicationFeeItem, appFeeItem2);

    assertThat(report.getPwaApplication()).isEqualTo(pwaApplicationDetail.getPwaApplication());
    assertThat(report.getTotalPennies()).isEqualTo(applicationFeeItem.getPennyAmount() + appFeeItem2.getPennyAmount());

  }

  @Test(expected = FeeException.class)
  public void getApplicationFeeReport_currentFeePeriodNotFound() {

    when(feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodStartTimestampIsBeforeAndPeriodEndTimestampIsAfter(any(),
        any()))
        .thenReturn(Optional.empty());
    var report = applicationFeeService.getApplicationFeeReport(pwaApplicationDetail);


  }
}