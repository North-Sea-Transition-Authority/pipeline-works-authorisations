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
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageAreaCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageCrossingUrlFactory;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;

/**
 * Construct summary of Carbon Storage Area Crossings Information for a given application.
 */
@Service
public class CarbonStorageAreaCrossingSummaryService implements ApplicationSectionSummariser {

  private final CarbonStorageAreaCrossingService crossingService;
  private final TaskListService taskListService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public CarbonStorageAreaCrossingSummaryService(
      CarbonStorageAreaCrossingService crossingService,
      TaskListService taskListService,
      PadFileManagementService padFileManagementService) {
    this.crossingService = crossingService;
    this.taskListService = taskListService;
    this.padFileManagementService = padFileManagementService;
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
        padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.CARBON_STORAGE_CROSSINGS));
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
