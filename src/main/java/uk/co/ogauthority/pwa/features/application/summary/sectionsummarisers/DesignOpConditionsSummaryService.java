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
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditionsService;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Construct summary of Design and Operating Conditions for a given application.
 */
@Service
public class DesignOpConditionsSummaryService implements ApplicationSectionSummariser {

  private final PadDesignOpConditionsService padDesignOpConditionsService;
  private final TaskListService taskListService;

  @Autowired
  public DesignOpConditionsSummaryService(
      PadDesignOpConditionsService padDesignOpConditionsService,
      TaskListService taskListService) {
    this.padDesignOpConditionsService = padDesignOpConditionsService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.DESIGN_OP_CONDITIONS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = ApplicationTask.DESIGN_OP_CONDITIONS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("designOpConditionsView", padDesignOpConditionsService.getDesignOpConditionsView(pwaApplicationDetail));
    summaryModel.put("unitMeasurements", UnitMeasurement.toMap());

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#designOpConditionsDetails"
        )),
        summaryModel
    );
  }


}
