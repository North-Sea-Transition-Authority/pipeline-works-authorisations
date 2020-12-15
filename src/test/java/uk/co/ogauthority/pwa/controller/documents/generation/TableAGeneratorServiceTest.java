package uk.co.ogauthority.pwa.controller.documents.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.service.documents.generation.TableAGeneratorService;
import uk.co.ogauthority.pwa.service.documents.views.tablea.DrawingForTableAView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.TableARowView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.TableAView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummary;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TableAGeneratorServiceTest {

  @Mock
  private PipelineDiffableSummaryService pipelineDiffableSummaryService;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  private PwaApplicationDetail pwaApplicationDetail;

  private TableAGeneratorService tableAGeneratorService;

  private static String PIPILINE_REF1 = "PL001";
  private static String PIPILINE_REF2 = "PL002";
  private static String PIPILINE_REF3 = "PL003";
  private static String PIPILINE_REF4 = "PL004";

  private static int IDENT_NO1 = 1;
  private static int IDENT_NO2 = 2;
  private static int IDENT_NO3 = 3;

  private static String DRAWING_REF1 = "Drawing Ref 1";
  private static String DRAWING_REF2 = "Drawing Ref 2";


  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL, 1, 1);
    tableAGeneratorService = new TableAGeneratorService(pipelineDiffableSummaryService, padProjectInformationService);
  }

  private PadPipeline createPadPipeline(String pipelineRef) {
    var pipeline = new Pipeline();
    pipeline.setId(1);
    PadPipeline padPipeline = null;
    try {
      padPipeline = PadPipelineTestUtil.createPadPipeline(pwaApplicationDetail, pipeline, PipelineType.PRODUCTION_FLOWLINE);
    } catch (IllegalAccessException ignored) {}
    padPipeline.setPipelineRef(pipelineRef);
    return padPipeline;
  }

  private PipelineHeaderView createHeaderView(PadPipeline padPipeline) {
    var padPipelineOverview = new PadPipelineOverview(padPipeline, 1L);
    return new PipelineHeaderView(padPipelineOverview);
  }

  private IdentView createIdentView(int identNumber, PadPipeline padPipeline) {
    PadPipelineIdent ident = null;
    try {
      ident = PadPipelineTestUtil.createPadPipelineident(padPipeline);
    } catch (IllegalAccessException ignored) {}
    ident.setIdentNo(identNumber);
    var identData = PadPipelineTestUtil.createPadPipelineIdentData(ident);
    return new IdentView(identData);
  }

  private PipelineDrawingSummaryView createDrawingSummaryView(String drawingRef) {
    var file = new PadFile(null, "1", null,  null);
    var technicalDrawing = new PadTechnicalDrawing(null, pwaApplicationDetail, file, drawingRef);
    return new PipelineDrawingSummaryView(technicalDrawing, List.of());
  }

  private DrawingForTableAView createDrawingForTableAView(
      List<PipelineDiffableSummary> pipelineDiffableSummaries, String projectName) {

    List<TableAView> tableAViews = new ArrayList<>();

    pipelineDiffableSummaries.forEach(summary -> {
      var headerRowView = new TableARowView(summary.getPipelineHeaderView());
      var identRowViews = summary.getIdentViews().stream().map(TableARowView::new).collect(
          Collectors.toList());
      tableAViews.add(new TableAView(
          summary.getPipelineHeaderView().getPipelineName(),
          headerRowView,
          identRowViews
      ));
    });

    return new DrawingForTableAView(
        tableAViews,
        projectName,
        pipelineDiffableSummaries.get(0).getDrawingSummaryView().getFileId(),
        pipelineDiffableSummaries.get(0).getDrawingSummaryView().getReference()
    );
  }



  @Test
  public void getDocumentSectionData_2DrawingsWithMultiplePipelines_unSortedPipelineNumbers() {

    //2 pipeline summaries for drawing 1
    var padPipeline4 = createPadPipeline(PIPILINE_REF4);
    var pipelineSummary1ForDrawing1 = PipelineDiffableSummary.from(
        createHeaderView(padPipeline4),
        List.of(createIdentView(IDENT_NO1, padPipeline4),
            createIdentView(IDENT_NO2, padPipeline4),
            createIdentView(IDENT_NO3, padPipeline4)),
        createDrawingSummaryView(DRAWING_REF1)
    );

    var padPipeline3 = createPadPipeline(PIPILINE_REF3);
    var pipelineSummary2ForDrawing1 = PipelineDiffableSummary.from(
        createHeaderView(padPipeline3),
        List.of(createIdentView(IDENT_NO1, padPipeline3),
            createIdentView(IDENT_NO2, padPipeline3),
            createIdentView(IDENT_NO3, padPipeline3)),
        createDrawingSummaryView(DRAWING_REF1)
    );

    //2 pipeline summaries for drawing 2
    var padPipeline2 = createPadPipeline(PIPILINE_REF2);
    var pipelineSummary1ForDrawing2 = PipelineDiffableSummary.from(
        createHeaderView(padPipeline2),
        List.of(createIdentView(IDENT_NO1, padPipeline2),
            createIdentView(IDENT_NO2, padPipeline2),
            createIdentView(IDENT_NO3, padPipeline2)),
        createDrawingSummaryView(DRAWING_REF2)
    );

    var padPipeline1 = createPadPipeline(PIPILINE_REF1);
    var pipelineSummary2ForDrawing2 = PipelineDiffableSummary.from(
        createHeaderView(padPipeline1),
        List.of(createIdentView(IDENT_NO1, padPipeline1),
            createIdentView(IDENT_NO2, padPipeline1),
            createIdentView(IDENT_NO3, padPipeline1)),
        createDrawingSummaryView(DRAWING_REF2)
    );

    when(pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail)).thenReturn(List.of(
        pipelineSummary1ForDrawing1,
        pipelineSummary2ForDrawing1,
        pipelineSummary1ForDrawing2,
        pipelineSummary2ForDrawing2));

    var projectInfo = new PadProjectInformation();
    projectInfo.setProjectName("project name");
    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail)).thenReturn(projectInfo);



    //assert that pipeline and drawing summaries have been mapped into TableA views
    // and ordered according to pipeline numbers within and across groups
    var documentSectionData = tableAGeneratorService.getDocumentSectionData(pwaApplicationDetail);
    var sectionName = documentSectionData.getTemplateModel().get("sectionName");
    var actualDrawingForTableAViews = (List<DrawingForTableAView>) documentSectionData.getTemplateModel().get("drawingForTableAViews");

    var expectedDrawing1ForTableAView = createDrawingForTableAView(
        List.of(pipelineSummary2ForDrawing1, pipelineSummary1ForDrawing1), projectInfo.getProjectName());

    var expectedDrawing2ForTableAView = createDrawingForTableAView(
        List.of(pipelineSummary2ForDrawing2, pipelineSummary1ForDrawing2), projectInfo.getProjectName());

    assertThat(sectionName).isEqualTo(DocumentSection.TABLE_A.getDisplayName());
    assertThat(actualDrawingForTableAViews).hasSize(2);
    assertThat(actualDrawingForTableAViews.get(0)).isEqualTo(expectedDrawing2ForTableAView);
    assertThat(actualDrawingForTableAViews.get(1)).isEqualTo(expectedDrawing1ForTableAView);
  }


}
