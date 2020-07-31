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
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;

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


    var newDetailList = permanentDepositService.getPermanentDepositViews(newPwaApplicationDetail);
    // TODO PWA-677 MH when I get back need to convert this so it uses the complex list diff functionality.
    //    var oldDetailList = newPwaApplicationDetail.getId().equals(oldPwaApplicationDetail.getId())
    //        ? newDetailList
    //        : permanentDepositService.getPermanentDepositViews(newPwaApplicationDetail);
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
