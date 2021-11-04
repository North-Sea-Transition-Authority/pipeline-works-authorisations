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
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Construct summary of confirmed option response for a given application.
 */
@Service
public class OptionConfirmationSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadConfirmationOfOptionService padConfirmationOfOptionService;

  @Autowired
  public OptionConfirmationSummaryService(TaskListService taskListService,
                                          PadConfirmationOfOptionService padConfirmationOfOptionService) {
    this.taskListService = taskListService;
    this.padConfirmationOfOptionService = padConfirmationOfOptionService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.CONFIRM_OPTIONS
    );

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {



    var view = padConfirmationOfOptionService.getPadConfirmationOfOptionView(pwaApplicationDetail);

    var sectionDisplayText = ApplicationTask.CONFIRM_OPTIONS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("view", view);

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#optionConfirmation"
        )),
        summaryModel
    );
  }


}
