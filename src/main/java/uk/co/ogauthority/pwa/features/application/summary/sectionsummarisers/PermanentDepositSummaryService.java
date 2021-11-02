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
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositOverview;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

@Service
public class PermanentDepositSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PermanentDepositService permanentDepositService;

  @Autowired
  public PermanentDepositSummaryService(
      TaskListService taskListService,
      PermanentDepositService permanentDepositService) {
    this.taskListService = taskListService;
    this.permanentDepositService = permanentDepositService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.PERMANENT_DEPOSITS,
        ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS
    );

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);

  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var newDetailList = permanentDepositService.getPermanentDepositViews(pwaApplicationDetail)
        .stream()
        .sorted(Comparator.comparing(PermanentDepositOverview::getDepositReference))
        .collect(Collectors.toList());


    var sectionDisplayText = ApplicationTask.PERMANENT_DEPOSITS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("depositList", newDetailList);
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
