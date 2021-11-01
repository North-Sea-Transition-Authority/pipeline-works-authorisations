package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.LocationDetailsUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetailsService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;

/**
 * Construct summary of Location Details for a given application.
 */
@Service
public class LocationDetailsSummaryService implements ApplicationSectionSummariser {

  private final PadLocationDetailsService padLocationDetailsService;
  private final TaskListService taskListService;

  @Autowired
  public LocationDetailsSummaryService(
      PadLocationDetailsService padLocationDetailsService,
      TaskListService taskListService) {
    this.padLocationDetailsService = padLocationDetailsService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.LOCATION_DETAILS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = ApplicationTask.LOCATION_DETAILS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("locationDetailsView", padLocationDetailsService.getLocationDetailsView(pwaApplicationDetail));
    summaryModel.put("locationDetailsUrlFactory", new LocationDetailsUrlFactory(pwaApplicationDetail));
    summaryModel.put("requiredQuestions", padLocationDetailsService.getRequiredQuestions(pwaApplicationDetail.getPwaApplicationType()));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#locationDetails"
        )),
        summaryModel
    );
  }


}
