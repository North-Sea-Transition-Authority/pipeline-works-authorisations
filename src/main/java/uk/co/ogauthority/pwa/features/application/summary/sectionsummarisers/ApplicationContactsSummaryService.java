package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.ContactTeamMemberView;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

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

    var pwaApplication = pwaApplicationDetail.getPwaApplication();
    var contactTeamMemberViews = pwaContactService.getContactsForPwaApplication(pwaApplication).stream()
        .map(contact -> pwaContactService.getTeamMemberView(pwaApplication, contact))
        .sorted(Comparator.comparing(ContactTeamMemberView::getFullName))
        .collect(Collectors.toList());

    var sectionDisplayText = ApplicationTask.APPLICATION_USERS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("contactTeamMemberViews", contactTeamMemberViews);

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
