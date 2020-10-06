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
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;

/**
 * Construct summary of HUOO information for a given application.
 */
@Service
public class HuooSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public HuooSummaryService(
      TaskListService taskListService,
      PadOrganisationRoleService padOrganisationRoleService) {
    this.taskListService = taskListService;
    this.padOrganisationRoleService = padOrganisationRoleService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.HUOO);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {



    var sectionDisplayText = ApplicationTask.HUOO.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("huooRolePipelineGroupsView",
        padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail));


    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#huooDetails"
        )),
        summaryModel
    );
  }




}
