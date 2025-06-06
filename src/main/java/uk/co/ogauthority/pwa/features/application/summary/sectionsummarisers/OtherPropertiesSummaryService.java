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
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PadPipelineOtherPropertiesService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Construct summary of Other Properties for a given application.
 */
@Service
public class OtherPropertiesSummaryService implements ApplicationSectionSummariser {

  private final PadPipelineOtherPropertiesService padPipelineOtherPropertiesService;
  private final TaskListService taskListService;

  @Autowired
  public OtherPropertiesSummaryService(
      PadPipelineOtherPropertiesService padPipelineOtherPropertiesService,
      TaskListService taskListService) {
    this.padPipelineOtherPropertiesService = padPipelineOtherPropertiesService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.PIPELINE_OTHER_PROPERTIES);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = ApplicationTask.PIPELINE_OTHER_PROPERTIES.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("otherPropertiesView", padPipelineOtherPropertiesService.getOtherPropertiesView(pwaApplicationDetail));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#otherPropertiesDetails"
        )),
        summaryModel
    );
  }


}
