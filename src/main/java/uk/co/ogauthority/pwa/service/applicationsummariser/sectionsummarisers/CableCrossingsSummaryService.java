package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CableCrossingUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossingService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;

/**
 * Construct summary of Cable Crossings for a given application.
 */
@Service
public class CableCrossingsSummaryService implements ApplicationSectionSummariser {

  private final PadCableCrossingService padCableCrossingService;
  private final TaskListService taskListService;
  private final PadFileService padFileService;

  @Autowired
  public CableCrossingsSummaryService(
      PadCableCrossingService padCableCrossingService,
      TaskListService taskListService, PadFileService padFileService) {
    this.padCableCrossingService = padCableCrossingService;
    this.taskListService = taskListService;
    this.padFileService = padFileService;
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
        padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.CABLE_CROSSINGS,
            ApplicationFileLinkStatus.FULL).stream()
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
