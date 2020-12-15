package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.DrawingForTableAView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.TableARowView;
import uk.co.ogauthority.pwa.service.documents.views.tablea.TableAView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentDiffableView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummary;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;

@Service
public class TableAGeneratorService implements DocumentSectionGenerator {


  private final PipelineDiffableSummaryService pipelineDiffableSummaryService;
  private final PadProjectInformationService padProjectInformationService;

  @Autowired
  public TableAGeneratorService(
      PipelineDiffableSummaryService pipelineDiffableSummaryService,
      PadProjectInformationService padProjectInformationService) {
    this.pipelineDiffableSummaryService = pipelineDiffableSummaryService;
    this.padProjectInformationService = padProjectInformationService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail) {

    var drawingForPipelineSummaryMap = getDrawingForPipelineSummaryMap(pwaApplicationDetail);
    var projectName = padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail).getProjectName();
    var drawingForTableAViews = mapDrawingsAndPipelinesToDrawingTableAView(projectName, drawingForPipelineSummaryMap);

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
    var applicationPipelineSummaryList = pipelineDiffableSummaryService.getApplicationDetailPipelines(
        pwaApplicationDetail);

    return applicationPipelineSummaryList.stream()
        .collect(Collectors.groupingBy(PipelineDiffableSummary::getDrawingSummaryView));
  }


  private List<DrawingForTableAView> mapDrawingsAndPipelinesToDrawingTableAView(
      String projectName,
      Map<PipelineDrawingSummaryView, List<PipelineDiffableSummary>> drawingForPipelineSummaryMap) {

    var drawingForTableAViews = drawingForPipelineSummaryMap.entrySet()
        .stream().map(entry -> {
          var drawingSummary = entry.getKey();
          var pipelineSummaries = entry.getValue();
          var tableAViews = pipelineSummaries.stream().map(pipelineSummary ->
              createTableAView(pipelineSummary.getPipelineHeaderView(), pipelineSummary.getIdentViews()))
              .sorted(Comparator.comparing(tableAView -> tableAView.getHeaderRow().getPipelineNumber()))
              .collect(Collectors.toList());

          return new DrawingForTableAView(
              tableAViews,
              projectName, drawingSummary.getFileId(),
              drawingSummary.getReference()
          );
        })
        .collect(Collectors.toList());

    return drawingForTableAViews;
  }


  private TableAView createTableAView(PipelineHeaderView headerView, List<IdentDiffableView> identViews) {
    var identTableARowViews = identViews.stream().map(TableARowView::new)
        .sorted(Comparator.comparing(TableARowView::getIdentNumber))
        .collect(Collectors.toList());
    var headerTableARowView = new TableARowView(headerView);
    return new TableAView(headerView.getPipelineName(), headerTableARowView, identTableARowViews);
  }

}
