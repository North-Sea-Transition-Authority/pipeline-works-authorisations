package uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CampaignWorksSummaryValidationResultTest {

  private CampaignWorksSummaryValidationResult campaignWorksSummaryValidationResult;


  private PwaApplicationDetail pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
      PwaApplicationType.INITIAL);

  private PadCampaignWorkSchedule padCampaignWorkSchedule1;
  private PadCampaignWorkSchedule padCampaignWorkSchedule2;


  @Spy
  private Function<PadCampaignWorkSchedule, Errors> scheduleValidationFunctionSpy;

  @Spy
  private  Function<PwaApplicationDetail, Boolean> allApplicationPipelinesScheduledCheckSpy;

  @Mock
  private Errors noErrors;

  @Mock
  private Errors hasErrors;

  @Before
  public void setup() {

    padCampaignWorkSchedule1 = new PadCampaignWorkSchedule();
    padCampaignWorkSchedule1.setId(1);
    padCampaignWorkSchedule1.setPwaApplicationDetail(pwaApplicationDetail);
    padCampaignWorkSchedule2 = new PadCampaignWorkSchedule();
    padCampaignWorkSchedule2.setId(2);
    padCampaignWorkSchedule2.setPwaApplicationDetail(pwaApplicationDetail);

    when(hasErrors.hasErrors()).thenReturn(true);
    when(scheduleValidationFunctionSpy.apply(any())).thenReturn(noErrors);
    when(allApplicationPipelinesScheduledCheckSpy.apply(any())).thenReturn(true);

  }


  @Test
  public void campaignWorksSummaryValidationResult_whenNoSchedules() {
    campaignWorksSummaryValidationResult = new CampaignWorksSummaryValidationResult(
        pwaApplicationDetail,
        List.of(),
        scheduleValidationFunctionSpy,
        allApplicationPipelinesScheduledCheckSpy
    );

    verifyNoInteractions(scheduleValidationFunctionSpy);
    verify(allApplicationPipelinesScheduledCheckSpy, times(1)).apply(eq(pwaApplicationDetail));

  }

  @Test
  public void campaignWorksSummaryValidationResult_whenSingleSchedule() {
    campaignWorksSummaryValidationResult = new CampaignWorksSummaryValidationResult(
        pwaApplicationDetail,
        List.of(padCampaignWorkSchedule1),
        scheduleValidationFunctionSpy,
        allApplicationPipelinesScheduledCheckSpy
    );

    verify(scheduleValidationFunctionSpy, times(1)).apply(eq(padCampaignWorkSchedule1));
    verify(allApplicationPipelinesScheduledCheckSpy, times(1)).apply(eq(pwaApplicationDetail));

  }

  @Test
  public void campaignWorksSummaryValidationResult_whenMultipleSchedule() {
    campaignWorksSummaryValidationResult = new CampaignWorksSummaryValidationResult(
        pwaApplicationDetail,
        List.of(padCampaignWorkSchedule1, padCampaignWorkSchedule2),
        scheduleValidationFunctionSpy,
        allApplicationPipelinesScheduledCheckSpy
    );

    verify(scheduleValidationFunctionSpy, times(1)).apply(eq(padCampaignWorkSchedule1));
    verify(scheduleValidationFunctionSpy, times(1)).apply(eq(padCampaignWorkSchedule2));
    verify(allApplicationPipelinesScheduledCheckSpy, times(1)).apply(eq(pwaApplicationDetail));

  }


  @Test
  public void isWorkScheduleInvalid_isAccurate() {

    when(scheduleValidationFunctionSpy.apply(padCampaignWorkSchedule1)).thenReturn(hasErrors);
    when(scheduleValidationFunctionSpy.apply(padCampaignWorkSchedule2)).thenReturn(noErrors);

    campaignWorksSummaryValidationResult = new CampaignWorksSummaryValidationResult(
        pwaApplicationDetail,
        List.of(padCampaignWorkSchedule1, padCampaignWorkSchedule2),
        scheduleValidationFunctionSpy,
        allApplicationPipelinesScheduledCheckSpy
    );

    assertThat(campaignWorksSummaryValidationResult.isWorkScheduleInvalid(padCampaignWorkSchedule1.getId())).isTrue();
    assertThat(campaignWorksSummaryValidationResult.isWorkScheduleInvalid(padCampaignWorkSchedule2.getId())).isFalse();

  }

  @Test
  public void isComplete_whenNoScheduleErrors_andAllPipelinesCheckPasses() {


    campaignWorksSummaryValidationResult = new CampaignWorksSummaryValidationResult(
        pwaApplicationDetail,
        List.of(padCampaignWorkSchedule1, padCampaignWorkSchedule2),
        scheduleValidationFunctionSpy,
        allApplicationPipelinesScheduledCheckSpy
    );

    assertThat(campaignWorksSummaryValidationResult.isComplete()).isTrue();


  }

  @Test
  public void isComplete_whenScheduleErrors_andAllPipelinesCheckPasses() {

    when(scheduleValidationFunctionSpy.apply(padCampaignWorkSchedule1)).thenReturn(hasErrors);
    campaignWorksSummaryValidationResult = new CampaignWorksSummaryValidationResult(
        pwaApplicationDetail,
        List.of(padCampaignWorkSchedule1, padCampaignWorkSchedule2),
        scheduleValidationFunctionSpy,
        allApplicationPipelinesScheduledCheckSpy
    );

    assertThat(campaignWorksSummaryValidationResult.isComplete()).isFalse();

  }

  @Test
  public void isComplete_whenNoScheduleErrors_andAllPipelinesCheckFails() {

    when(allApplicationPipelinesScheduledCheckSpy.apply(any())).thenReturn(false);

    campaignWorksSummaryValidationResult = new CampaignWorksSummaryValidationResult(
        pwaApplicationDetail,
        List.of(padCampaignWorkSchedule1, padCampaignWorkSchedule2),
        scheduleValidationFunctionSpy,
        allApplicationPipelinesScheduledCheckSpy
    );

    assertThat(campaignWorksSummaryValidationResult.isComplete()).isFalse();

  }


}