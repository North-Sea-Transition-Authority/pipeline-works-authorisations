package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory;

/**
 * Construct summary of Licence Block Information for a given application.
 */
@Service
public class LicenceBlockSummaryService implements ApplicationSectionSummariser {

  private final BlockCrossingService blockCrossingService;
  private final PadFileService padFileService;
  private final TaskListService taskListService;

  @Autowired
  public LicenceBlockSummaryService(
      BlockCrossingService blockCrossingService,
      PadFileService padFileService, TaskListService taskListService) {
    this.blockCrossingService = blockCrossingService;
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

    var sectionDisplayText = CrossingAgreementTask.LICENCE_AND_BLOCKS.getDisplayText();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("blockCrossingViews", blockCrossingService.getCrossedBlockViews(pwaApplicationDetail));
    summaryModel.put("blockCrossingFileViews",
        padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.BLOCK_CROSSINGS,
            ApplicationFileLinkStatus.FULL));
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
