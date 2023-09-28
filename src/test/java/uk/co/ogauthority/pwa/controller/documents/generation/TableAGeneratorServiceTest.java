package uk.co.ogauthority.pwa.controller.documents.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineOverview;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.IdentView;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineDiffableSummary;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.service.documents.generation.ConsentDocumentImageService;
import uk.co.ogauthority.pwa.service.documents.generation.TableAGeneratorService;
import uk.co.ogauthority.pwa.service.documents.views.tablea.DrawingForTableAView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.TableARowView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.TableAView;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TableAGeneratorServiceTest {

  @Mock
  private PipelineDiffableSummaryService pipelineDiffableSummaryService;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private ConsentDocumentImageService consentDocumentImageService;

  @Mock
  private PadTechnicalDrawingService padTechnicalDrawingService;

  @Mock
  private MarkdownService markdownService;

  private PwaApplicationDetail pwaApplicationDetail;

  private TableAGeneratorService tableAGeneratorService;

  private static String PIPILINE_REF1 = "PL001";
  private static String PIPILINE_REF2 = "PL002";
  private static String PIPILINE_REF3 = "PL003";
  private static String PIPILINE_REF4 = "PL004";
  private static String PIPILINE_REF5 = "PL005";

  private static int IDENT_NO1 = 1;
  private static int IDENT_NO2 = 2;
  private static int IDENT_NO3 = 3;

  private static String DRAWING_REF1 = "Drawing Ref 1";
  private static String DRAWING_REF2 = "Drawing Ref 2";

  private static String FILE_ID1 = "1";
  private static String FILE_ID2 = "2";

  private static String IMG_SRC1 = "source 1 url";
  private static String IMG_SRC2 = "source 2 url";

  private PadProjectInformation projectInfo;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL, 1, 1);
    tableAGeneratorService = new TableAGeneratorService(
        pipelineDiffableSummaryService,
        padProjectInformationService,
        consentDocumentImageService,
        padTechnicalDrawingService,
        markdownService);

    projectInfo = new PadProjectInformation();
    projectInfo.setProjectName("project name");
    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail)).thenReturn(projectInfo);

    doAnswer(invocation -> {
      var passedArg = (String) invocation.getArgument(0);
      return passedArg + "markdownconverted";
    }).when(markdownService).convertMarkdownToHtml(any());

  }

  private PadPipeline createPadPipeline(String pipelineRef) {
    var pipeline = new Pipeline();
    pipeline.setId(1);
    PadPipeline padPipeline = null;
    try {
      padPipeline = PadPipelineTestUtil.createPadPipeline(pwaApplicationDetail, pipeline, PipelineType.PRODUCTION_FLOWLINE);
    } catch (IllegalAccessException ignored) {}
    padPipeline.setPipelineRef(pipelineRef);
    padPipeline.setFootnote("some footnote text");
    return padPipeline;
  }

  private PipelineHeaderView createHeaderView(PadPipeline padPipeline) {
    var padPipelineOverview = new PadPipelineOverview(padPipeline, 1L);
    return new PipelineHeaderView(padPipelineOverview, null, null);
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

  private PipelineDrawingSummaryView createDrawingSummaryView(String drawingRef, String fileId) {
    var file = new PadFile(null, fileId, null,  null);
    var uploadedFileView = new UploadedFileView(fileId, null, 1L, null, null, null);
    var technicalDrawing = new PadTechnicalDrawing(null, pwaApplicationDetail, file, drawingRef);
    return new PipelineDrawingSummaryView(technicalDrawing, List.of(), uploadedFileView);
  }

  private DrawingForTableAView createDrawingForTableAView(
      List<PipelineDiffableSummary> pipelineDiffableSummaries, String projectName, String imgSrc) {

    List<TableAView> tableAViews = new ArrayList<>();

    pipelineDiffableSummaries.forEach(summary -> {
      var headerRowView = new TableARowView(summary.getPipelineHeaderView());
      var identRowViews = summary.getIdentViews().stream().map(TableARowView::new).collect(
          Collectors.toList());
      tableAViews.add(new TableAView(
          summary.getPipelineHeaderView().getPipelineName(),
          headerRowView,
          identRowViews,
          markdownService.convertMarkdownToHtml(summary.getPipelineHeaderView().getFootnote())));
    });

    return new DrawingForTableAView(
        tableAViews,
        projectName,
        pipelineDiffableSummaries.get(0).getDrawingSummaryView().getFileId(),
        pipelineDiffableSummaries.get(0).getDrawingSummaryView().getReference(),
        imgSrc);
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
        createDrawingSummaryView(DRAWING_REF1, FILE_ID1)
    );

    var padPipeline3 = createPadPipeline(PIPILINE_REF3);
    var pipelineSummary2ForDrawing1 = PipelineDiffableSummary.from(
        createHeaderView(padPipeline3),
        List.of(createIdentView(IDENT_NO1, padPipeline3),
            createIdentView(IDENT_NO2, padPipeline3),
            createIdentView(IDENT_NO3, padPipeline3)),
        createDrawingSummaryView(DRAWING_REF1, FILE_ID1)
    );

    //2 pipeline summaries for drawing 2
    var padPipeline2 = createPadPipeline(PIPILINE_REF2);
    var pipelineSummary1ForDrawing2 = PipelineDiffableSummary.from(
        createHeaderView(padPipeline2),
        List.of(createIdentView(IDENT_NO1, padPipeline2),
            createIdentView(IDENT_NO2, padPipeline2),
            createIdentView(IDENT_NO3, padPipeline2)),
        createDrawingSummaryView(DRAWING_REF2, FILE_ID2)
    );

    var padPipeline1 = createPadPipeline(PIPILINE_REF1);
    var pipelineSummary2ForDrawing2 = PipelineDiffableSummary.from(
        createHeaderView(padPipeline1),
        List.of(createIdentView(IDENT_NO1, padPipeline1),
            createIdentView(IDENT_NO2, padPipeline1),
            createIdentView(IDENT_NO3, padPipeline1)),
        createDrawingSummaryView(DRAWING_REF2, FILE_ID2)
    );

    //1 RTS Pipeline
    var padPipeline5 = createPadPipeline(PIPILINE_REF5);
    padPipeline5.setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);
    var pipelineSummary3NoDrawing = PipelineDiffableSummary.from(
        createHeaderView(padPipeline5),
        List.of(createIdentView(IDENT_NO1, padPipeline5),
            createIdentView(IDENT_NO2, padPipeline5)),
        null
    );

    when(consentDocumentImageService.convertFilesToImageSourceMap(Set.of(FILE_ID1))).thenReturn(
        Map.of(FILE_ID1, IMG_SRC1));
    when(consentDocumentImageService.convertFilesToImageSourceMap(Set.of(FILE_ID2))).thenReturn(
        Map.of(FILE_ID2, IMG_SRC2));

    when(pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail)).thenReturn(List.of(
        pipelineSummary1ForDrawing1,
        pipelineSummary2ForDrawing1,
        pipelineSummary1ForDrawing2,
        pipelineSummary2ForDrawing2,
        pipelineSummary3NoDrawing));

    when(padTechnicalDrawingService.isDrawingRequiredForPipeline(PipelineStatus.IN_SERVICE)).thenReturn(true);
    when(padTechnicalDrawingService.isDrawingRequiredForPipeline(PipelineStatus.RETURNED_TO_SHORE)).thenReturn(false);

    //assert that pipeline and drawing summaries have been mapped into TableA views
    // and ordered according to pipeline numbers within and across groups
    // and that any RTS & NL pipelines are excluded
    var documentSectionData = tableAGeneratorService.getDocumentSectionData(pwaApplicationDetail, null, DocGenType.PREVIEW);
    var sectionName = documentSectionData.getTemplateModel().get("sectionName");
    var actualDrawingForTableAViews = (List<DrawingForTableAView>) documentSectionData.getTemplateModel().get("drawingForTableAViews");

    var expectedDrawing1ForTableAView = createDrawingForTableAView(
        List.of(pipelineSummary2ForDrawing1, pipelineSummary1ForDrawing1), projectInfo.getProjectName(), IMG_SRC1);

    var expectedDrawing2ForTableAView = createDrawingForTableAView(
        List.of(pipelineSummary2ForDrawing2, pipelineSummary1ForDrawing2), projectInfo.getProjectName(), IMG_SRC2);

    assertThat(sectionName).isEqualTo(DocumentSection.TABLE_A.getDisplayName());
    assertThat(actualDrawingForTableAViews).hasSize(2);
    assertThat(actualDrawingForTableAViews.get(0)).isEqualTo(expectedDrawing2ForTableAView);
    assertThat(actualDrawingForTableAViews.get(1)).isEqualTo(expectedDrawing1ForTableAView);
  }

  @Test
  public void getDocumentSectionData_noPipelines() {

    when(pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail)).thenReturn(List.of());

    var docSectionData = tableAGeneratorService.getDocumentSectionData(pwaApplicationDetail, null, DocGenType.PREVIEW);

    assertThat(docSectionData).isNull();

  }

}
