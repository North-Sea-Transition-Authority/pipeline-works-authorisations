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
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfoService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Construct summary of General Technical Information for a given application.
 */
@Service
public class GeneralTechInfoSummaryService implements ApplicationSectionSummariser {

  private final PadPipelineTechInfoService padPipelineTechInfoService;
  private final TaskListService taskListService;

  @Autowired
  public GeneralTechInfoSummaryService(
      PadPipelineTechInfoService padPipelineTechInfoService,
      TaskListService taskListService) {
    this.padPipelineTechInfoService = padPipelineTechInfoService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.GENERAL_TECH_DETAILS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = ApplicationTask.GENERAL_TECH_DETAILS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("generalTechInfoView", padPipelineTechInfoService.getGeneralTechInfoView(pwaApplicationDetail));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#generalTechInfoDetails"
        )),
        summaryModel
    );
  }


}
