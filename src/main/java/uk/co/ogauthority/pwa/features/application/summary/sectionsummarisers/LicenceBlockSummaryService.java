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
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingUrlFactory;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;

/**
 * Construct summary of Licence Block Information for a given application.
 */
@Service
public class LicenceBlockSummaryService implements ApplicationSectionSummariser {

  private final BlockCrossingService blockCrossingService;
  private final TaskListService taskListService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public LicenceBlockSummaryService(
      BlockCrossingService blockCrossingService,
      TaskListService taskListService,
      PadFileManagementService padFileManagementService) {
    this.blockCrossingService = blockCrossingService;
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

    var sectionDisplayText = CrossingAgreementTask.LICENCE_AND_BLOCKS.getDisplayText();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("blockCrossingViews", blockCrossingService.getCrossedBlockViews(pwaApplicationDetail));
    summaryModel.put("blockCrossingFileViews",
        padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.BLOCK_CROSSINGS));
    summaryModel.put("blockCrossingUrlFactory", new BlockCrossingUrlFactory(pwaApplicationDetail));
    summaryModel.put("isDocumentsRequired", blockCrossingService.isDocumentsRequired(pwaApplicationDetail));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#licenceBlockDetails"
        )),
        summaryModel
    );
  }


}
