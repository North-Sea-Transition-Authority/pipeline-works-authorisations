package uk.co.ogauthority.pwa.features.feemanagement.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeeItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeeItemRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriod;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetail;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailFeeItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailItemRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.form.feeperiod.FeePeriodForm;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class FeePeriodServiceTest {

  @Mock
  private FeePeriodRepository feePeriodRepository;

  @Mock
  private FeePeriodDetailRepository feePeriodDetailRepository;

  @Mock
  private FeePeriodDetailItemRepository feePeriodDetailItemRepository;

  @Mock
  private FeeItemRepository feeItemRepository;

  private FeePeriodService feePeriodService;

  private FeePeriodForm form;

  @Captor
  ArgumentCaptor<FeePeriodDetail> periodCaptor;

  @Captor
  ArgumentCaptor<List<FeePeriodDetailFeeItem>> costMapCaptor;

  @Before
  public void setup() {
    feePeriodService = new FeePeriodService(feePeriodRepository,
        feePeriodDetailRepository,
        feePeriodDetailItemRepository,
        feeItemRepository);

    form = new FeePeriodForm();
    form.setPeriodDescription("Test Description");
    form.setPeriodStartDate("11/07/2000");

    var applicationCostMap = new HashMap<String, String>();
    applicationCostMap.put(PwaApplicationType.INITIAL.name() + ":" + PwaApplicationFeeType.DEFAULT.name(), "500.00");
    form.setApplicationCostMap(applicationCostMap);

    when(feePeriodRepository.save(any())).thenReturn(getSavedFeePeriod());
    when(feeItemRepository.findByPwaApplicationTypeAndPwaApplicationFeeType(any(), any())).thenReturn(Optional.of(getFeeItem()));
  }

  @Test
  public void testSaveFeePeriod_GetFeePeriodObjects() {
    feePeriodService.saveFeePeriod(form, getTestPerson());
    verify(feePeriodDetailRepository).save(periodCaptor.capture());

    var actualFeePeriodDetail = periodCaptor.getValue();
    var expectedFeeDetail = new FeePeriodDetail();
    expectedFeeDetail.setFeePeriod(getSavedFeePeriod());
    expectedFeeDetail.setPeriodStartTimestamp(DateUtils.datePickerStringToInstant("11/07/2000"));
    expectedFeeDetail.setTipFlag(true);

    assertThat(actualFeePeriodDetail).isEqualTo(expectedFeeDetail);
  }

  @Test
  public void testSaveFeePeriod_GetFeePeriodDetailItems() {
    feePeriodService.saveFeePeriod(form, getTestPerson());
    verify(feePeriodDetailItemRepository).saveAll(costMapCaptor.capture());

    var actualCostMap = costMapCaptor.getValue();
    var expectedFeeDetailItem = new FeePeriodDetailFeeItem();
    expectedFeeDetailItem.setFeePeriodDetail(getSavedFeePeriodDetail());
    expectedFeeDetailItem.setFeeItem(getFeeItem());
    expectedFeeDetailItem.setPennyAmount(50000);

    assertThat(actualCostMap.get(0)).isEqualTo(expectedFeeDetailItem);
  }

  @Test
  public void testSaveFeePeriod_NewPeriodNoneExisting() {
    feePeriodService.saveFeePeriod(form, getTestPerson());
    verify(feePeriodDetailRepository).save(any());
  }

  @Test
  public void testSaveFeePeriod_NewPendingActiveExisting() {
    when(feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodEndTimestampIsNull())
        .thenReturn(Optional.of(getActiveFeePeriodDetail()));
    feePeriodService.saveFeePeriod(form, getTestPerson());
    verify(feePeriodDetailRepository, times(3)).save(periodCaptor.capture());
    var captorValues = periodCaptor.getAllValues();

    assertThat(captorValues.stream()
        .map(FeePeriodDetail::getPeriodStartTimestamp)
        .collect(Collectors.toList()))
        .contains(DateUtils.datePickerStringToInstant("11/07/2000"));

    assertThat(captorValues.stream()
        .map(FeePeriodDetail::getPeriodEndTimestamp)
        .collect(Collectors.toList()))
        .contains(DateUtils.datePickerStringToInstant("10/07/2000").plusSeconds(86399));
  }

  @Test
  public void testSaveFeePeriod_EditPendingNoActive() {
    when(feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodEndTimestampIsNull())
        .thenReturn(Optional.of(getSavedFeePeriodDetail()));
    when(feePeriodDetailRepository.findByTipFlagIsTrueAndFeePeriod(any()))
        .thenReturn(Optional.of(getSavedFeePeriodDetail()));

    feePeriodService.saveFeePeriod(form, getTestPerson());
    verify(feePeriodDetailRepository, times(4)).save(periodCaptor.capture());

    var captorValues = periodCaptor.getAllValues();
    assertThat(captorValues.stream()
        .map(FeePeriodDetail::getPeriodStartTimestamp)
        .collect(Collectors.toList()))
        .contains(DateUtils.datePickerStringToInstant("11/07/2000"));

    assertThat(captorValues.stream()
        .map(FeePeriodDetail::getPeriodEndTimestamp)
        .collect(Collectors.toList()))
        .contains(DateUtils.datePickerStringToInstant("10/07/2000").plusSeconds(86399));
  }

  @Test
  public void testSaveFeePeriod_EditPendingActiveExisting() {
    when(feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodEndTimestampIsNull())
        .thenReturn(Optional.of(getSavedFeePeriodDetail()));
    when(feePeriodDetailRepository.findByTipFlagIsTrueAndFeePeriod(any()))
        .thenReturn(Optional.of(getSavedFeePeriodDetail()));
    when(feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodEndTimestamp(any()))
        .thenReturn(Optional.of(getSavedFeePeriodDetail()));

    feePeriodService.saveFeePeriod(form, getTestPerson());
    verify(feePeriodDetailRepository, times(6)).save(periodCaptor.capture());

    var captorValues = periodCaptor.getAllValues();
    assertThat(captorValues.stream()
        .map(FeePeriodDetail::getPeriodStartTimestamp)
        .collect(Collectors.toList()))
        .contains(DateUtils.datePickerStringToInstant("11/07/2000"));

    assertThat(captorValues.stream()
        .map(FeePeriodDetail::getPeriodEndTimestamp)
        .collect(Collectors.toList()))
        .contains(DateUtils.datePickerStringToInstant("10/07/2000").plusSeconds(86399));
  }

  private FeePeriod getSavedFeePeriod() {
    var period = new FeePeriod();
    period.setId(1000);
    period.setDescription("Test Description");

    return period;
  }

  private FeePeriodDetail getSavedFeePeriodDetail() {
    var periodDetail = new FeePeriodDetail();
    periodDetail.setId(1000);
    periodDetail.setFeePeriod(getSavedFeePeriod());
    periodDetail.setPeriodStartTimestamp(DateUtils.datePickerStringToInstant("11/07/2000"));
    periodDetail.setTipFlag(true);

    return periodDetail;
  }

  private FeePeriodDetail getActiveFeePeriodDetail() {
    var periodDetail = new FeePeriodDetail();
    periodDetail.setId(100);
    periodDetail.setFeePeriod(getSavedFeePeriod());
    periodDetail.setPeriodStartTimestamp(DateUtils.datePickerStringToInstant("11/07/1998"));
    periodDetail.setTipFlag(true);

    return periodDetail;
  }

  private FeeItem getFeeItem() {
    var feeItem = new FeeItem();
    feeItem.setPwaApplicationType(PwaApplicationType.INITIAL);
    feeItem.setPwaApplicationFeeType(PwaApplicationFeeType.DEFAULT);

    return feeItem;
  }

  private Person getTestPerson() {
    return new Person(
        10001,
        "Test",
        "Tester",
        "TestTester@testing.com",
        "07840885992");
  }
}
