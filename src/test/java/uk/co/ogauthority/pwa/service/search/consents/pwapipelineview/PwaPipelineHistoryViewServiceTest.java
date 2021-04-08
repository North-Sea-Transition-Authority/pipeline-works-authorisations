package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.PipelinesSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.testutil.PwaPipelineViewTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaPipelineHistoryViewServiceTest {

  @Mock
  private PipelineDiffableSummaryService pipelineDiffableSummaryService;

  @Mock
  private PipelinesSummaryService pipelinesSummaryService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  private PwaPipelineHistoryViewService pwaPipelineHistoryViewService;

  private static PipelineId PIPELINE_ID = new PipelineId(1);
  private static int PIPELINE_DETAIL_ID1 = 1;
  private static int PIPELINE_DETAIL_ID2 = 2;
  private static int PIPELINE_DETAIL_ID3 = 3;

  private static Instant TODAY_AFTERNOON;
  private static Instant TODAY_MORNING;
  private static Instant YESTERDAY;


  @Before
  public void setUp() throws Exception {
    pwaPipelineHistoryViewService = new PwaPipelineHistoryViewService(pipelineDiffableSummaryService,
        pipelinesSummaryService, pipelineDetailService);

    var today = LocalDate.now().atStartOfDay();
    TODAY_AFTERNOON = today.plusHours(13).atZone(ZoneId.systemDefault()).toInstant();
    TODAY_MORNING = today.plusHours(5).atZone(ZoneId.systemDefault()).toInstant();
    YESTERDAY = today.minusDays(1).atZone(ZoneId.systemDefault()).toInstant();
  }

  @Test
  public void getPipelinesVersionSearchSelectorItems_onlyPipelinesChangedOnSameDayHaveOrderTag_itemsAreOrderedLatestFirst() {

    var pipelineDetailCreatedTodayAfternoon = PwaPipelineViewTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID3, PIPELINE_ID, TODAY_AFTERNOON);
    var pipelineDetailCreatedTodayMorning = PwaPipelineViewTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID2, PIPELINE_ID, TODAY_MORNING);
    var pipelineDetailCreatedYesterday = PwaPipelineViewTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID1, PIPELINE_ID, YESTERDAY);

    when(pipelineDetailService.getAllPipelineDetailsForPipeline(PIPELINE_ID)).thenReturn(
        List.of(pipelineDetailCreatedYesterday, pipelineDetailCreatedTodayAfternoon, pipelineDetailCreatedTodayMorning));

    var pipelinesVersionSearchSelectorItems = pwaPipelineHistoryViewService.getPipelinesVersionSearchSelectorItems(PIPELINE_ID.asInt());

    assertThat(pipelinesVersionSearchSelectorItems.keySet()).containsExactly(
        pipelineDetailCreatedTodayAfternoon.getId().toString(), pipelineDetailCreatedTodayMorning.getId().toString(), pipelineDetailCreatedYesterday.getId().toString()
    );

    var expectedOrderTagNumber = 2;
    assertThat(pipelinesVersionSearchSelectorItems.get(pipelineDetailCreatedTodayAfternoon.getId().toString())).contains(
        String.format("%s (%s)", DateUtils.formatDate(pipelineDetailCreatedTodayAfternoon.getStartTimestamp()), expectedOrderTagNumber));

    expectedOrderTagNumber = 1;
    assertThat(pipelinesVersionSearchSelectorItems.get(pipelineDetailCreatedTodayMorning.getId().toString())).isEqualTo(
        String.format("%s (%s)", DateUtils.formatDate(pipelineDetailCreatedTodayMorning.getStartTimestamp()), expectedOrderTagNumber));

    assertThat(pipelinesVersionSearchSelectorItems.get(pipelineDetailCreatedYesterday.getId().toString())).isEqualTo(
        DateUtils.formatDate(pipelineDetailCreatedYesterday.getStartTimestamp()));

  }

  @Test
  public void getPipelinesVersionSearchSelectorItems_consentReferenceDisplayedWhenAvailable_onlyLatestPipelineVersionHasLatestVersionText() {
    
    var pipelineDetailNoConsentRef = PwaPipelineViewTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID2, PIPELINE_ID, YESTERDAY);
    var pipelineDetailHasConsentRef = PwaPipelineViewTestUtil.createPipelineDetail(
        PIPELINE_DETAIL_ID1, PIPELINE_ID, TODAY_MORNING, PwaPipelineViewTestUtil.createPwaConsent("178/V/11"));

    when(pipelineDetailService.getAllPipelineDetailsForPipeline(PIPELINE_ID)).thenReturn(
        List.of(pipelineDetailHasConsentRef, pipelineDetailNoConsentRef));

    var pipelinesVersionSearchSelectorItems = pwaPipelineHistoryViewService.getPipelinesVersionSearchSelectorItems(PIPELINE_ID.asInt());

    assertThat(pipelinesVersionSearchSelectorItems.get(pipelineDetailHasConsentRef.getId().toString())).isEqualTo(
        String.format("Latest version (%s - %s)", DateUtils.formatDate(pipelineDetailHasConsentRef.getStartTimestamp()), pipelineDetailHasConsentRef.getPwaConsent().getReference()));

    assertThat(pipelinesVersionSearchSelectorItems.get(pipelineDetailNoConsentRef.getId().toString())).isEqualTo(
        DateUtils.formatDate(pipelineDetailNoConsentRef.getStartTimestamp()));
  }


  @Test
  public void getPipelineSummary() {

    var summary = PwaPipelineViewTestUtil.createPipelineDiffableSummary(PIPELINE_DETAIL_ID1);
    when(pipelineDiffableSummaryService.getConsentedPipeline(PIPELINE_DETAIL_ID1)).thenReturn(summary);

    pwaPipelineHistoryViewService.getDiffedPipelineSummaryModel(PIPELINE_DETAIL_ID1);
    verify(pipelinesSummaryService, times(1)).produceDiffedPipelineModel(summary, summary);
  }



}