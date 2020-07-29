package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSummariserUtil;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;

@Service
public class ProjectInformationSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;

  @Autowired
  public ProjectInformationSummaryService(
      TaskListService taskListService) {
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail newPwaApplicationDetail,
                              PwaApplicationDetail oldPwaApplicationDetail) {

    var taskNameFilter = Set.of(
        ApplicationTask.PROJECT_INFORMATION.getDisplayName());

    return ApplicationSummariserUtil.canSummariseOptimised(newPwaApplicationDetail, oldPwaApplicationDetail,
        (pwaApplicationDetail -> taskListService.getPrepareAppTasks(newPwaApplicationDetail)
            .stream()
            .anyMatch(o -> taskNameFilter.contains(o.getTaskName()))));
  }

  @Override
  public ApplicationSectionSummary summariseDifferences(PwaApplicationDetail newPwaApplicationDetail,
                                                        PwaApplicationDetail oldPwaApplicationDetail,
                                                        String templateName) {
    var sectionDisplayText = ApplicationTask.PROJECT_INFORMATION.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#projectInformation"
        )),
        summaryModel
    );
  }
}
