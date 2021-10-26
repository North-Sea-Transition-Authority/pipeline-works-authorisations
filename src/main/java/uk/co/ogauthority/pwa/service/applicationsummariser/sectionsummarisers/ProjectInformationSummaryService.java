package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;

@Service
public class ProjectInformationSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadProjectInformationService padProjectInformationService;

  @Autowired
  public ProjectInformationSummaryService(
      TaskListService taskListService,
      PadProjectInformationService padProjectInformationService) {
    this.taskListService = taskListService;
    this.padProjectInformationService = padProjectInformationService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.PROJECT_INFORMATION);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = ApplicationTask.PROJECT_INFORMATION.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("projectInfoView", padProjectInformationService.getProjectInformationView(pwaApplicationDetail));
    summaryModel.put("requiredQuestions", padProjectInformationService.getRequiredQuestions(pwaApplicationDetail.getPwaApplicationType()));

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
