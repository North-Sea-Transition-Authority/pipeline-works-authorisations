package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.IdentDiffableView;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineDiffableSummary;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.DrawingForTableAView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.TableARowView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.TableAView;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;
import uk.co.ogauthority.pwa.util.pipelines.PipelineNumberSortingUtil;

@Service
public class TableAGeneratorService implements DocumentSectionGenerator {


  private final PipelineDiffableSummaryService pipelineDiffableSummaryService;
  private final PadProjectInformationService padProjectInformationService;
  private final ConsentDocumentImageService consentDocumentImageService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final MarkdownService markdownService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public TableAGeneratorService(
      PipelineDiffableSummaryService pipelineDiffableSummaryService,
      PadProjectInformationService padProjectInformationService,
      ConsentDocumentImageService consentDocumentImageService,
      PadTechnicalDrawingService padTechnicalDrawingService,
      MarkdownService markdownService,
      PadFileManagementService padFileManagementService
  ) {
    this.pipelineDiffableSummaryService = pipelineDiffableSummaryService;
    this.padProjectInformationService = padProjectInformationService;
    this.consentDocumentImageService = consentDocumentImageService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.markdownService = markdownService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                    DocumentInstance documentInstance,
                                                    DocGenType docGenType) {

    var drawingForPipelineSummaryMap = getDrawingForPipelineSummaryMap(pwaApplicationDetail);
    var projectName = padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail).getProjectName();
    var drawingForTableAViews = mapDrawingsAndPipelinesToDrawingTableAView(projectName, drawingForPipelineSummaryMap, pwaApplicationDetail);

    // if no pipelines or drawings, nothing to show, exit early
    if (drawingForTableAViews.isEmpty()) {
      return null;
    }

    drawingForTableAViews.sort(Comparator.comparing(drawingForTableA ->
        drawingForTableA.getTableAViews().get(0).getHeaderRow().getPipelineNumber()));

    Map<String, Object> modelMap = Map.of(
        "sectionName", DocumentSection.TABLE_A.getDisplayName(),
        "drawingForTableAViews", drawingForTableAViews
    );

    return new DocumentSectionData("documents/consents/sections/tableAs", modelMap);
  }


  private Map<PipelineDrawingSummaryView, List<PipelineDiffableSummary>> getDrawingForPipelineSummaryMap(
      PwaApplicationDetail pwaApplicationDetail) {
    var applicationPipelineSummaryList = pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail)
        .stream()
        .filter(diffableSummary -> padTechnicalDrawingService.isDrawingRequiredForPipeline(
            diffableSummary.getPipelineHeaderView().getPipelineStatus()))
        .collect(Collectors.toList());

    return applicationPipelineSummaryList.stream()
        .collect(Collectors.groupingBy(PipelineDiffableSummary::getDrawingSummaryView));
  }


  private List<DrawingForTableAView> mapDrawingsAndPipelinesToDrawingTableAView(
      String projectName,
      Map<PipelineDrawingSummaryView,
      List<PipelineDiffableSummary>> drawingForPipelineSummaryMap,
      PwaApplicationDetail pwaApplicationDetail
  ) {

    return drawingForPipelineSummaryMap.entrySet()
        .stream().map(entry -> {
          var drawingSummary = entry.getKey();
          var pipelineSummaries = entry.getValue();
          var tableAViews = pipelineSummaries.stream()
              .map(pipelineSummary ->
                  createTableAView(pipelineSummary.getPipelineHeaderView(), pipelineSummary.getIdentViews()))
              .sorted((view1, view2) -> PipelineNumberSortingUtil.compare(
                  view1.getHeaderRow().getPipelineNumber(), view2.getHeaderRow().getPipelineNumber()))
              .collect(Collectors.toList());

          return new DrawingForTableAView(
              tableAViews,
              projectName,
              drawingSummary.getFileId(),
              drawingSummary.getReference(),
              getImgSource(drawingSummary.getFileId(), pwaApplicationDetail));
        })
        .collect(Collectors.toList());
  }

  private String getImgSource(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    return consentDocumentImageService.convertFileToImageSource(
        padFileManagementService.getUploadedFile(pwaApplicationDetail, UUID.fromString(fileId))
    );
  }


  private TableAView createTableAView(PipelineHeaderView headerView, List<IdentDiffableView> identViews) {

    var identTableARowViews = identViews.stream().map(TableARowView::new)
        .sorted(Comparator.comparing(TableARowView::getIdentNumber))
        .collect(Collectors.toList());

    var headerTableARowView = new TableARowView(headerView);

    var footnoteText = markdownService.convertMarkdownToHtml(headerView.getFootnote());

    return new TableAView(headerView.getPipelineName(), headerTableARowView, identTableARowViews, footnoteText);

  }

}
