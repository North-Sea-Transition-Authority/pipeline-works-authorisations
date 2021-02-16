package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetailFeeItem;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.FeePeriodDetailItemRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.FeePeriodDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
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
  private FeePeriodDetailItemRepository feePeriodDetailItemRepository;

  private Clock clock = Clock.fixed(
      LocalDateTime.of(2020, 12, 1, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault());


  private ApplicationFeeService applicationFeeService;

  private PwaApplicationDetail pwaApplicationDetail;

  private FeePeriodDetail feePeriodDetail;

  private FeePeriodDetailFeeItem feePeriodDetailFeeItem;

  @Before
  public void setUp() throws Exception {


    feePeriodDetail = FeePeriodTestUtil.createDefaultFeePeriodDetail();
    feePeriodDetailFeeItem = FeePeriodTestUtil.createFeePeriodFeeItem(feePeriodDetail, FEE_DESC, FEE_AMOUNT);

    when(feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodStartTimestampIsBeforeAndPeriodEndTimestampIsAfter(any(),
        any()))
        .thenReturn(Optional.of(feePeriodDetail));
    when(feePeriodDetailItemRepository.findAllByFeePeriodDetailAndFeeItem_PwaApplicationType(any(), any()))
        .thenReturn(List.of(feePeriodDetailFeeItem));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setAppReference(APP_REF);

    applicationFeeService = new ApplicationFeeService(
        feePeriodDetailRepository,
        feePeriodDetailItemRepository,
        clock
    );
  }

  @Test
  public void getApplicationFeeReport_currentFeePeriod_withChargeItemForAppType_exists_notFastTrack() {

    var report = applicationFeeService.getApplicationFeeReport(pwaApplicationDetail);

    ArgumentCaptor<Instant> maxPeriodInstantCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(feePeriodDetailRepository, times(1))
        .findByTipFlagIsTrueAndPeriodStartTimestampIsBeforeAndPeriodEndTimestampIsAfter(eq(clock.instant()),
            maxPeriodInstantCaptor.capture());

    // confirm that the default period end date provided when is suitably far away
    var maxDateMinimum = LocalDate.of(4000, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    assertThat(maxPeriodInstantCaptor.getValue()).isAfterOrEqualTo(maxDateMinimum);

    assertThat(report.getApplicationFeeItems())
        .containsExactly(new ApplicationFeeItem(FEE_DESC, FEE_AMOUNT));
    assertThat(report.getPwaApplication()).isEqualTo(pwaApplicationDetail.getPwaApplication());
    assertThat(report.getFeeSummary()).containsOnlyOnce(APP_REF);
    assertThat(report.getFeeSummary()).doesNotContain(FAST_TRACK_STRING);
    assertThat(report.getTotalPennies()).isEqualTo(FEE_AMOUNT);

  }

  @Test
  public void getApplicationFeeReport_currentFeePeriod_withChargeItemForAppType_exists_fastTrack() {

    pwaApplicationDetail.setSubmittedAsFastTrackFlag(true);

    var report = applicationFeeService.getApplicationFeeReport(pwaApplicationDetail);

    assertThat(report.getApplicationFeeItems()).hasSize(2)
        .allSatisfy(applicationFeeItem -> assertThat(applicationFeeItem.getPennyAmount()).isEqualTo(FEE_AMOUNT))
        .anySatisfy(applicationFeeItem -> assertThat(applicationFeeItem.getDescription()).isEqualTo(FEE_DESC))
        .anySatisfy(applicationFeeItem -> assertThat(applicationFeeItem.getDescription()).contains(FAST_TRACK_STRING));

    assertThat(report.getPwaApplication()).isEqualTo(pwaApplicationDetail.getPwaApplication());
    assertThat(report.getFeeSummary()).containsOnlyOnce(APP_REF);
    assertThat(report.getFeeSummary()).containsOnlyOnce(FAST_TRACK_STRING);
    assertThat(report.getTotalPennies()).isEqualTo(FEE_AMOUNT * 2);

  }

  @Test(expected = FeeException.class)
  public void getApplicationFeeReport_currentFeePeriodNotFound() {

    when(feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodStartTimestampIsBeforeAndPeriodEndTimestampIsAfter(any(),
        any()))
        .thenReturn(Optional.empty());
    var report = applicationFeeService.getApplicationFeeReport(pwaApplicationDetail);


  }
}