package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineDiffableSummaryServiceTest {

  private static final int PIPELINE_ID = 1;
//  private static final int PAD_PIPELINE_ID = 20;
  private static final String PAD_PIPELINE_NAME = "PAD_PIPELINE_NAME";

  private static final String PIPELINE_POINT_1 = "POINT_1";
  private static final String PIPELINE_POINT_2 = "POINT_2";
  private static final String PIPELINE_POINT_3 = "POINT_3";
  private static final String PIPELINE_POINT_4 = "POINT_4";

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PipelineOverview padPipelineOverview;

  private PipelineDiffableSummaryService pipelineDiffableSummaryService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Mock
  private IdentView identStart;

  @Mock
  private IdentView identMid;

  @Mock
  private IdentView identEnd;

  @Before
  public void setup() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(padPipelineOverview.getPipelineName()).thenReturn(PAD_PIPELINE_NAME);
    when(padPipelineOverview.getPipelineId()).thenReturn(PIPELINE_ID);

    IdentViewTestUtil.setupSingleCoreIdentViewMock(identStart, PIPELINE_POINT_1, PIPELINE_POINT_2, 1);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(identMid, PIPELINE_POINT_2, PIPELINE_POINT_3, 2);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(identEnd, PIPELINE_POINT_3, PIPELINE_POINT_4, 3);

    pipelineDiffableSummaryService = new PipelineDiffableSummaryService(
        padPipelineService,
        padPipelineIdentService);
  }


  @Test
  public void getApplicationDetailPipelines_whenNoPipelines() {

    assertThat(pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail)).isEmpty();

  }

  @Test
  public void getApplicationDetailPipelines_whenOnePipeline_andZeroIdents() {

    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineOverview));
//    when(padPipelineIdentService.getIdentViewsFromOverview(padPipelineOverview))
//        .thenReturn(List.of(identStart, identMid, identEnd));

    var summaryList = pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail);

    assertThat(summaryList).hasSize(1);
    var summary = summaryList.get(0);

    assertThat(summary.getPipelineId().asInt()).isEqualTo(PIPELINE_ID);
    assertThat(summary.getPipelineName()).isEqualTo(PAD_PIPELINE_NAME);
    assertThat(summary.getIdentViews()).hasSize(0);


  }

  @Test
  public void getApplicationDetailPipelines_whenOnePipeline_andMultipleIdents_thenMappedAsExpected() {

    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineOverview));
    when(padPipelineIdentService.getIdentViewsFromOverview(padPipelineOverview))
        .thenReturn(List.of(identStart, identMid, identEnd));

    var summaryList = pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail);
    var summary = summaryList.get(0);

    assertThat(summary.getIdentViews()).hasSize(3);
    assertThat(summary.getIdentViews().get(0).getFromLocation()).isEqualTo(PIPELINE_POINT_1);
    assertThat(summary.getIdentViews().get(0).getToLocation()).isEqualTo(PIPELINE_POINT_2);
    assertThat(summary.getIdentViews().get(0).getConnectedToPrevious()).isFalse();
    assertThat(summary.getIdentViews().get(0).getConnectedToNext()).isTrue();

    assertThat(summary.getIdentViews().get(1).getFromLocation()).isEqualTo(PIPELINE_POINT_2);
    assertThat(summary.getIdentViews().get(1).getToLocation()).isEqualTo(PIPELINE_POINT_3);
    assertThat(summary.getIdentViews().get(1).getConnectedToPrevious()).isTrue();
    assertThat(summary.getIdentViews().get(1).getConnectedToNext()).isTrue();

    assertThat(summary.getIdentViews().get(2).getFromLocation()).isEqualTo(PIPELINE_POINT_3);
    assertThat(summary.getIdentViews().get(2).getToLocation()).isEqualTo(PIPELINE_POINT_4);
    assertThat(summary.getIdentViews().get(2).getConnectedToPrevious()).isTrue();
    assertThat(summary.getIdentViews().get(2).getConnectedToNext()).isFalse();

  }




}