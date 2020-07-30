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
public class PermanentDepositSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;

  @Autowired
  public PermanentDepositSummaryService(
      TaskListService taskListService) {
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail newPwaApplicationDetail,
                              PwaApplicationDetail oldPwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.PERMANENT_DEPOSITS,
        ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS
    );

    return ApplicationSummariserUtil.canSummariseOptimised(newPwaApplicationDetail, oldPwaApplicationDetail,
        (pwaApplicationDetail -> taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail)));

  }

  @Override
  public ApplicationSectionSummary summariseDifferences(PwaApplicationDetail newPwaApplicationDetail,
                                                        PwaApplicationDetail oldPwaApplicationDetail,
                                                        String templateName) {
    var sectionDisplayText = ApplicationTask.PERMANENT_DEPOSITS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#permanentDeposits"
        )),
        summaryModel
    );
  }
}
