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
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PadPartnerLettersService;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PartnerApprovalLettersUrlFactory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Construct summary of Partner Approval Letters for a given application.
 */
@Service
public class PartnerApprovalLettersSummaryService implements ApplicationSectionSummariser {

  private final PadPartnerLettersService padPartnerLettersService;
  private final TaskListService taskListService;

  @Autowired
  public PartnerApprovalLettersSummaryService(
      PadPartnerLettersService padPartnerLettersService,
      TaskListService taskListService) {
    this.padPartnerLettersService = padPartnerLettersService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.PARTNER_LETTERS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = ApplicationTask.PARTNER_LETTERS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("partnerLettersView", padPartnerLettersService.getPartnerLettersView(pwaApplicationDetail));
    summaryModel.put("partnerLettersUrlFactory", new PartnerApprovalLettersUrlFactory(pwaApplicationDetail));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#partnerLettersDetails"
        )),
        summaryModel
    );
  }


}
