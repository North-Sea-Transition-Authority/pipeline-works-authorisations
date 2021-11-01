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
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PipelineCrossingUrlFactory;
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
 * Construct summary of Pipeline Crossings Information for a given application.
 */
@Service
public class PipelineCrossingsSummaryService implements ApplicationSectionSummariser {

  private final PadPipelineCrossingService padPipelineCrossingService;
  private final PadFileService padFileService;
  private final TaskListService taskListService;

  @Autowired
  public PipelineCrossingsSummaryService(
      PadPipelineCrossingService padPipelineCrossingService,
      PadFileService padFileService, TaskListService taskListService) {
    this.padPipelineCrossingService = padPipelineCrossingService;
    this.padFileService = padFileService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.CROSSING_AGREEMENTS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail)
        && padPipelineCrossingService.canShowInTaskList(pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = CrossingAgreementTask.PIPELINE_CROSSINGS.getDisplayText();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("pipelineCrossingViews", padPipelineCrossingService.getPipelineCrossingViews(pwaApplicationDetail));
    summaryModel.put("pipelineCrossingUrlFactory", new PipelineCrossingUrlFactory(pwaApplicationDetail));
    summaryModel.put("pipelineCrossingFiles",
            padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.PIPELINE_CROSSINGS,
                ApplicationFileLinkStatus.FULL)
                .stream()
              .sorted(Comparator.comparing(UploadedFileView::getFileName))
              .collect(Collectors.toList()));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#pipelineCrossingDetails"
        )),
        summaryModel
    );
  }


}