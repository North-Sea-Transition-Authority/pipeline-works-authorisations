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
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.WorkScheduleView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Construct summary of Campaign Work Schedule for a given application.
 */
@Service
public class CampaignWorkScheduleSummaryService implements ApplicationSectionSummariser {

  private final CampaignWorksService campaignWorksService;
  private final TaskListService taskListService;

  @Autowired
  public CampaignWorkScheduleSummaryService(
      CampaignWorksService campaignWorksService,
      TaskListService taskListService) {
    this.campaignWorksService = campaignWorksService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.CAMPAIGN_WORKS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = ApplicationTask.CAMPAIGN_WORKS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("workScheduleViews", campaignWorksService.getWorkScheduleViews(pwaApplicationDetail)
        .stream()
        .sorted(Comparator.comparing(WorkScheduleView::getWorkStartDate))
        .collect(Collectors.toList()));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#workScheduleDetails"
        )),
        summaryModel
    );
  }


}
