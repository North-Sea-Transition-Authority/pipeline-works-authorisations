package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.UmbilicalCrossSectionUrlFactory;

/**
 * Construct summary of technical drawings for a given application.
 */
@Service
public class TechnicalDrawingsSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadFileService padFileService;

  @Autowired
  public TechnicalDrawingsSummaryService(
      TaskListService taskListService,
      PadFileService padFileService) {
    this.taskListService = taskListService;
    this.padFileService = padFileService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.TECHNICAL_DRAWINGS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {


    var sectionDisplayText = "Other diagrams";
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("admiraltyChartFileViews",
        padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.ADMIRALTY_CHART, ApplicationFileLinkStatus.FULL));
    summaryModel.put("admiraltyChartUrlFactory", new AdmiraltyChartUrlFactory(pwaApplicationDetail));
    summaryModel.put("umbilicalFileViews",
        padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.UMBILICAL_CROSS_SECTION,
            ApplicationFileLinkStatus.FULL));
    summaryModel.put("umbilicalUrlFactory", new UmbilicalCrossSectionUrlFactory(pwaApplicationDetail));
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
