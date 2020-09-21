package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;

/**
 * Construct summary of Environmental and Decommissioning information for a given application.
 */
@Service
public class EnvironmentalDecomSummaryService implements ApplicationSectionSummariser {

  private final PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private final TaskListService taskListService;

  @Autowired
  public EnvironmentalDecomSummaryService(
      PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService,
      TaskListService taskListService) {
    this.padEnvironmentalDecommissioningService = padEnvironmentalDecommissioningService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("environmentalDecommView",
        padEnvironmentalDecommissioningService.getEnvironmentalDecommissioningView(pwaApplicationDetail));
    summaryModel.put("environmentalConditions", EnvironmentalCondition.stream()
        .sorted(Comparator.comparing(EnvironmentalCondition::getDisplayOrder))
        .collect(Collectors.toList()));
    summaryModel.put("decommissioningConditions", DecommissioningCondition.stream()
        .sorted(Comparator.comparing(DecommissioningCondition::getDisplayOrder))
        .collect(Collectors.toList()));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#environmentalDecommDetails"
        )),
        summaryModel
    );
  }


}
