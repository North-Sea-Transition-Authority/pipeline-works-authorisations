package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers.PipelinesSummaryService;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.testutil.PwaPipelineViewTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class PwaPipelineHistoryViewServiceTest {

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


  @BeforeEach
  void setUp() throws Exception {
    pwaPipelineHistoryViewService = new PwaPipelineHistoryViewService(pipelineDiffableSummaryService,
        pipelinesSummaryService, pipelineDetailService);

    var today = LocalDate.now().atStartOfDay();
    TODAY_AFTERNOON = today.plusHours(13).atZone(ZoneId.systemDefault()).toInstant();
    TODAY_MORNING = today.plusHours(5).atZone(ZoneId.systemDefault()).toInstant();
    YESTERDAY = today.minusDays(1).atZone(ZoneId.systemDefault()).toInstant();
  }

  @Test
  void getPipelinesVersionSearchSelectorItems_onlyPipelinesChangedOnSameDayHaveOrderTag_itemsAreOrderedLatestFirst() {

    var pipelineDetailCreatedTodayAfternoon = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID3, PIPELINE_ID, TODAY_AFTERNOON);
    var pipelineDetailCreatedTodayMorning = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID2, PIPELINE_ID, TODAY_MORNING);
    var pipelineDetailCreatedYesterday = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID1, PIPELINE_ID, YESTERDAY);

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
    assertThat(pipelinesVersionSearchSelectorItems).contains(
        entry(pipelineDetailCreatedTodayMorning.getId().toString(),
            String.format("%s (%s)", DateUtils.formatDate(pipelineDetailCreatedTodayMorning.getStartTimestamp()), expectedOrderTagNumber)),

        entry(pipelineDetailCreatedYesterday.getId().toString(),
            DateUtils.formatDate(pipelineDetailCreatedYesterday.getStartTimestamp()))
    );

  }

  @Test
  void getPipelinesVersionSearchSelectorItems_consentReferenceDisplayedWhenAvailable_onlyLatestPipelineVersionHasLatestVersionText() {

    var pipelineDetailNoConsentRef = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID2, PIPELINE_ID, YESTERDAY);
    var pipelineDetailHasConsentRef = PipelineDetailTestUtil.createPipelineDetail(
        PIPELINE_DETAIL_ID1, PIPELINE_ID, TODAY_MORNING, PwaPipelineViewTestUtil.createPwaConsent("178/V/11"));

    when(pipelineDetailService.getAllPipelineDetailsForPipeline(PIPELINE_ID)).thenReturn(
        List.of(pipelineDetailHasConsentRef, pipelineDetailNoConsentRef));

    var pipelinesVersionSearchSelectorItems = pwaPipelineHistoryViewService.getPipelinesVersionSearchSelectorItems(PIPELINE_ID.asInt());

    assertThat(pipelinesVersionSearchSelectorItems).contains(
        entry(pipelineDetailHasConsentRef.getId().toString(),
            String.format("Latest version (%s - %s)",
            DateUtils.formatDate(pipelineDetailHasConsentRef.getStartTimestamp()), pipelineDetailHasConsentRef.getPwaConsent().getReference())),

        entry(pipelineDetailNoConsentRef.getId().toString(), DateUtils.formatDate(pipelineDetailNoConsentRef.getStartTimestamp()))
    );
  }


  @Test
  void getDiffedPipelineSummaryModel_hasPreviousVersion_selectedVersionDiffedAgainstPrevious() {

    var pipelineDetailForSelectedVersion = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID1, PIPELINE_ID, TODAY_MORNING);
    var pipelineDetailForPreviousVersion = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID2, PIPELINE_ID, YESTERDAY);
    when(pipelineDetailService.getAllPipelineDetailsForPipeline(PIPELINE_ID)).thenReturn(
        List.of(pipelineDetailForSelectedVersion, pipelineDetailForPreviousVersion));

    var summaryForSelectedVersion = PwaPipelineViewTestUtil.createPipelineDiffableSummary(PIPELINE_ID.asInt());
    when(pipelineDiffableSummaryService.getConsentedPipelineDetailSummary(PIPELINE_DETAIL_ID1)).thenReturn(summaryForSelectedVersion);

    var summaryForPreviousVersion = PwaPipelineViewTestUtil.createPipelineDiffableSummary(PIPELINE_ID.asInt());
    when(pipelineDiffableSummaryService.getConsentedPipelineDetailSummary(PIPELINE_DETAIL_ID2)).thenReturn(summaryForPreviousVersion);

    pwaPipelineHistoryViewService.getDiffedPipelineSummaryModel(PIPELINE_DETAIL_ID1, PIPELINE_ID.asInt());
    verify(pipelinesSummaryService, times(1)).produceDiffedPipelineModel(summaryForSelectedVersion, summaryForPreviousVersion);
  }


  @Test
  void getDiffedPipelineSummaryModel_doesNotHavePreviousVersion_selectedVersionDiffedAgainstItself() {

    var pipelineDetailForSelectedVersion = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID1, PIPELINE_ID, TODAY_MORNING);
    when(pipelineDetailService.getAllPipelineDetailsForPipeline(PIPELINE_ID)).thenReturn(
        List.of(pipelineDetailForSelectedVersion));

    var summaryForSelectedVersion = PwaPipelineViewTestUtil.createPipelineDiffableSummary(PIPELINE_ID.asInt());
    when(pipelineDiffableSummaryService.getConsentedPipelineDetailSummary(PIPELINE_DETAIL_ID1)).thenReturn(summaryForSelectedVersion);

    pwaPipelineHistoryViewService.getDiffedPipelineSummaryModel(PIPELINE_DETAIL_ID1, PIPELINE_ID.asInt());
    verify(pipelinesSummaryService, times(1)).produceDiffedPipelineModel(summaryForSelectedVersion, summaryForSelectedVersion);
  }



}