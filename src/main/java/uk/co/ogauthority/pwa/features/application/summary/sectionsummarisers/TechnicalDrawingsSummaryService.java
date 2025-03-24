package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionUrlFactory;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Construct summary of technical drawings for a given application.
 */
@Service
public class TechnicalDrawingsSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final AdmiraltyChartFileService admiraltyChartFileService;
  private final UmbilicalCrossSectionService umbilicalCrossSectionService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public TechnicalDrawingsSummaryService(
      TaskListService taskListService,
      AdmiraltyChartFileService admiraltyChartFileService,
      UmbilicalCrossSectionService umbilicalCrossSectionService,
      PadFileManagementService padFileManagementService) {
    this.taskListService = taskListService;
    this.admiraltyChartFileService = admiraltyChartFileService;
    this.umbilicalCrossSectionService = umbilicalCrossSectionService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.TECHNICAL_DRAWINGS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail)
        && (admiraltyChartFileService.canUploadDocuments(pwaApplicationDetail)
        || umbilicalCrossSectionService.canUploadDocuments(pwaApplicationDetail));
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {
    var sectionDisplayText = "Other diagrams";
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("admiraltyChartFileViews", padFileManagementService
        .getUploadedFileViews(pwaApplicationDetail, FileDocumentType.ADMIRALTY_CHART));
    summaryModel.put("admiraltyChartUrlFactory", new AdmiraltyChartUrlFactory(pwaApplicationDetail));
    summaryModel.put("umbilicalFileViews",padFileManagementService
        .getUploadedFileViews(pwaApplicationDetail, FileDocumentType.UMBILICAL_CROSS_SECTION));
    summaryModel.put("umbilicalUrlFactory", new UmbilicalCrossSectionUrlFactory(pwaApplicationDetail));
    summaryModel.put("canShowAdmiraltyChart", admiraltyChartFileService.canUploadDocuments(pwaApplicationDetail));
    summaryModel.put("canShowUmbilicalCrossSection", umbilicalCrossSectionService.canUploadDocuments(pwaApplicationDetail));
    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#technicalDrawings"
        )),
        summaryModel
    );
  }
}
