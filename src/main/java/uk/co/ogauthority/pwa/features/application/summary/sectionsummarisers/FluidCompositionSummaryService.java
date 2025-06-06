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
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfoService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Construct summary of Fluid Composition information for a given application.
 */
@Service
public class FluidCompositionSummaryService implements ApplicationSectionSummariser {

  private final PadFluidCompositionInfoService padFluidCompositionInfoService;
  private final TaskListService taskListService;

  @Autowired
  public FluidCompositionSummaryService(
      PadFluidCompositionInfoService padFluidCompositionInfoService,
      TaskListService taskListService) {
    this.padFluidCompositionInfoService = padFluidCompositionInfoService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.FLUID_COMPOSITION);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = ApplicationTask.FLUID_COMPOSITION.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("fluidCompositionView", padFluidCompositionInfoService.getFluidCompositionView(pwaApplicationDetail));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#fluidCompositionDetails"
        )),
        summaryModel
    );
  }


}
