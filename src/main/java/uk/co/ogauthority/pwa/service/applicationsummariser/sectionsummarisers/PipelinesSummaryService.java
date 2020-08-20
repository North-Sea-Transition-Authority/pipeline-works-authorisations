package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummaryService;

/**
 * Construct summary of pipelines for a given application.
 */
@Service
public class PipelinesSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PipelineDiffableSummaryService pipelineDiffableSummaryService;

  @Autowired
  public PipelinesSummaryService(
      TaskListService taskListService,
      PipelineDiffableSummaryService pipelineDiffableSummaryService) {
    this.taskListService = taskListService;
    this.pipelineDiffableSummaryService = pipelineDiffableSummaryService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.PIPELINES);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {
    var sectionDisplayText = ApplicationTask.PIPELINES.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("pipelines", pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail));
    summaryModel.put("unitMeasurements", UnitMeasurement.toMap());
    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#pipelinesHeader"
        )),
        summaryModel
    );
  }
}
