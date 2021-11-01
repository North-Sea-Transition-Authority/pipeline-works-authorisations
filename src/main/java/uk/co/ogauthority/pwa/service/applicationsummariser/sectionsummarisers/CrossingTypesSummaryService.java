package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTaskService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.types.CrossingTypesService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.tasklist.CrossingTaskGeneralPurposeTaskAdapter;

/**
 * Construct summary of Types of crossings Information for a given application.
 */
@Service
public class CrossingTypesSummaryService implements ApplicationSectionSummariser {

  private final CrossingTypesService crossingTypesService;
  private final TaskListService taskListService;
  private final ApplicationTaskService applicationTaskService;

  @Autowired
  public CrossingTypesSummaryService(CrossingTypesService crossingTypesService,
                                     TaskListService taskListService,
                                     ApplicationTaskService applicationTaskService) {
    this.crossingTypesService = crossingTypesService;
    this.taskListService = taskListService;
    this.applicationTaskService = applicationTaskService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.CROSSING_AGREEMENTS);

    var crossingTypeTaskAdapter = new CrossingTaskGeneralPurposeTaskAdapter(CrossingAgreementTask.CROSSING_TYPES);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail)
        && applicationTaskService.canShowTask(crossingTypeTaskAdapter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = CrossingAgreementTask.CROSSING_TYPES.getDisplayText();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("crossingTypesView", crossingTypesService.getCrossingTypesView(pwaApplicationDetail));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#crossingTypeDetails"
        )),
        summaryModel
    );
  }


}
