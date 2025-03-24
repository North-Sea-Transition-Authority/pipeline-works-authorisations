package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CableCrossingUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossingService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;

/**
 * Construct summary of Cable Crossings for a given application.
 */
@Service
public class CableCrossingsSummaryService implements ApplicationSectionSummariser {

  private final PadCableCrossingService padCableCrossingService;
  private final TaskListService taskListService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public CableCrossingsSummaryService(
      PadCableCrossingService padCableCrossingService,
      TaskListService taskListService,
      PadFileManagementService padFileManagementService) {
    this.padCableCrossingService = padCableCrossingService;
    this.taskListService = taskListService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.CROSSING_AGREEMENTS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail)
        && padCableCrossingService.canShowInTaskList(pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = CrossingAgreementTask.CABLE_CROSSINGS.getDisplayText();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("cableCrossingViews", padCableCrossingService.getCableCrossingViews(pwaApplicationDetail));
    summaryModel.put("cableCrossingUrlFactory", new CableCrossingUrlFactory(pwaApplicationDetail));
    summaryModel.put("cableCrossingFiles",
        padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.CABLE_CROSSINGS).stream()
            .sorted(Comparator.comparing(UploadedFileView::getFileName))
            .collect(Collectors.toList()));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#cableCrossingDetails"
        )),
        summaryModel
    );
  }


}
