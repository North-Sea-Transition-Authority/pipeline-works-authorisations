package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionUrlFactory;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;

/**
 * Construct summary of technical drawings for a given application.
 */
@Service
public class TechnicalDrawingsSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadFileService padFileService;
  private final AdmiraltyChartFileService admiraltyChartFileService;
  private final UmbilicalCrossSectionService umbilicalCrossSectionService;

  @Autowired
  public TechnicalDrawingsSummaryService(
      TaskListService taskListService,
      PadFileService padFileService,
      AdmiraltyChartFileService admiraltyChartFileService,
      UmbilicalCrossSectionService umbilicalCrossSectionService) {
    this.taskListService = taskListService;
    this.padFileService = padFileService;
    this.admiraltyChartFileService = admiraltyChartFileService;
    this.umbilicalCrossSectionService = umbilicalCrossSectionService;
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
    summaryModel.put("admiraltyChartFileViews", padFileService
            .getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART, ApplicationFileLinkStatus.FULL));
    summaryModel.put("admiraltyChartUrlFactory", new AdmiraltyChartUrlFactory(pwaApplicationDetail));
    summaryModel.put("umbilicalFileViews",
        padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.UMBILICAL_CROSS_SECTION,
            ApplicationFileLinkStatus.FULL));
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
