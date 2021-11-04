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
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingsService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Construct summary of permanent deposit drawings for a given application.
 */
@Service
public class DepositDrawingsSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final DepositDrawingsService depositDrawingsService;

  @Autowired
  public DepositDrawingsSummaryService(
      TaskListService taskListService,
      DepositDrawingsService depositDrawingsService) {
    this.taskListService = taskListService;
    this.depositDrawingsService = depositDrawingsService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {


    var sectionDisplayText = ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("depositDrawingViews", depositDrawingsService.getDepositDrawingSummaryViews(pwaApplicationDetail));
    summaryModel.put("depositDrawingUrlFactory",
        new DepositDrawingUrlFactory(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId()));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#depositDrawingDetails"
        )),
        summaryModel
    );
  }



}
