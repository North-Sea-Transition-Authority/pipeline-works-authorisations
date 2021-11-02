package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;

/**
 * Construct summary of application contacts for a given application.
 */
@Service
public class ApplicationContactsSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PwaContactService pwaContactService;

  @Autowired
  public ApplicationContactsSummaryService(TaskListService taskListService,
                                           PwaContactService pwaContactService) {
    this.taskListService = taskListService;
    this.pwaContactService = pwaContactService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.APPLICATION_USERS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    List<TeamMemberView> teamMemberViews = pwaContactService.getContactsForPwaApplication(pwaApplicationDetail.getPwaApplication()).stream()
        .map(contact -> pwaContactService.getTeamMemberView(pwaApplicationDetail.getPwaApplication(), contact))
        .sorted(Comparator.comparing(TeamMemberView::getFullName))
        .collect(Collectors.toList());

    var sectionDisplayText = ApplicationTask.APPLICATION_USERS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("teamMemberViews", teamMemberViews);

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#appContactDetails"
        )),
        summaryModel
    );
  }



}
