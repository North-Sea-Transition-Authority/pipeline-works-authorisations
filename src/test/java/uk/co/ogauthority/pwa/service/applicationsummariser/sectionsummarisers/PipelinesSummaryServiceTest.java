package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentViewTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummary;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.UmbilicalCrossSectionUrlFactory;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelinesSummaryServiceTest {


  private static final int PIPELINE_ID = 1;
  private static final String PIPELINE_NAME = "PAD_PIPELINE_NAME";

  private static final String PIPELINE_POINT_1 = "POINT_1";
  private static final String PIPELINE_POINT_2 = "POINT_2";
  private static final String PIPELINE_POINT_3 = "POINT_3";
  private static final String PIPELINE_POINT_4 = "POINT_4";

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PipelineDiffableSummaryService pipelineDiffableSummaryService;

  @Mock
  private DiffService diffService;

  @Mock
  private PipelineOverview pipelineOverview;

  @Mock
  private PipelineHeaderView pipelineHeaderView;

  @Mock
  private IdentView identStart;

  @Mock
  private IdentView identMid;

  @Mock
  private IdentView identEnd;


  private PipelinesSummaryService pipelinesSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    when(pipelineHeaderView.getPipelineId()).thenReturn(PIPELINE_ID);

    IdentViewTestUtil.setupSingleCoreIdentViewMock(identStart, PIPELINE_POINT_1, PIPELINE_POINT_2, 1);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(identMid, PIPELINE_POINT_2, PIPELINE_POINT_3, 2);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(identEnd, PIPELINE_POINT_3, PIPELINE_POINT_4, 3);

    pipelinesSummaryService = new PipelinesSummaryService(
        taskListService,
        pipelineDiffableSummaryService,
        diffService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);

  }

  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(pipelinesSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(pipelinesSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(pipelinesSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    var appSummary = pipelinesSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(3);
    assertThat(appSummary.getTemplateModel()).contains(entry("unitMeasurements", UnitMeasurement.toMap()));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.PIPELINES.getDisplayName()));
    assertThat(appSummary.getTemplateModel()).contains(entry("pipelines", List.of()));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.PIPELINES.getDisplayName(), "#pipelinesHeader")
    );

    verify(pipelineDiffableSummaryService, times(1)).getApplicationDetailPipelines(pwaApplicationDetail);

  }

  @Test
  public void getDiffedPipelineSummaryList_serviceInteractions_whenSingleAppPipelineAdded() {

    var appPipelineSummary = PipelineDiffableSummary.from(pipelineHeaderView, List.of(identStart, identMid, identEnd));
    var diffedSummaryList = pipelinesSummaryService.getDiffedPipelineSummaryList(List.of(appPipelineSummary), List.of());

    assertThat(diffedSummaryList).hasSize(1);
    assertThat(diffedSummaryList.get(0)).containsOnlyKeys("pipelineHeader", "pipelineIdents");

    verify(diffService, times(1)).diff(
        eq(appPipelineSummary.getPipelineHeaderView()),
        eq(new PipelineHeaderView()),
        eq(Set.of("identViews")));

    verify(diffService, times(1)).diffComplexLists(
        eq(appPipelineSummary.getIdentViews()),
        eq(PipelineDiffableSummary.empty().getIdentViews()),
        any(), // how can we test what lambda Function are given?
        any());

  }

}