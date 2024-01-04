package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageAreaCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageCrossingUrlFactory;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;

/**
 * Construct summary of Carbon Storage Area Crossings Information for a given application.
 */
@Service
public class CarbonStorageAreaCrossingSummaryService implements ApplicationSectionSummariser {

  private final CarbonStorageAreaCrossingService crossingService;
  private final PadFileService padFileService;
  private final TaskListService taskListService;

  @Autowired
  public CarbonStorageAreaCrossingSummaryService(
      CarbonStorageAreaCrossingService crossingService,
      PadFileService padFileService,
      TaskListService taskListService) {
    this.crossingService = crossingService;
    this.padFileService = padFileService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.CROSSING_AGREEMENTS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = CrossingAgreementTask.CARBON_STORAGE_AREAS.getDisplayText();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("carbonStorageCrossingViews", crossingService.getCrossedAreaViews(pwaApplicationDetail));
    summaryModel.put("carbonStorageCrossingFileViews",
        padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.CARBON_STORAGE_CROSSINGS,
            ApplicationFileLinkStatus.FULL));
    summaryModel.put("carbonStorageCrossingUrlFactory", new CarbonStorageCrossingUrlFactory(pwaApplicationDetail));
    summaryModel.put("isDocumentsRequired", crossingService.isDocumentsRequired(pwaApplicationDetail));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#carbonStorageCrossingDetails"
        )),
        summaryModel
    );
  }


}
