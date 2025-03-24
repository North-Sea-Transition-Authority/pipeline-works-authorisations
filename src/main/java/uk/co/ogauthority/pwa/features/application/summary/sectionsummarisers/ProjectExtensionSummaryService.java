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
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

@Service
public class ProjectExtensionSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;

  private final PadFileManagementService padFileManagementService;

  @Autowired
  public ProjectExtensionSummaryService(TaskListService taskListService, PadFileManagementService padFileManagementService) {
    this.taskListService = taskListService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {
    return taskListService.anyTaskShownForApplication(Set.of(ApplicationTask.PROJECT_EXTENSION), pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail, String templateName) {
    var sectionDisplayText = ApplicationTask.PROJECT_EXTENSION.getDisplayName();
    var permissionFile = padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.PROJECT_EXTENSION);

    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("permissionFile", permissionFile.isEmpty() ? null : permissionFile.getFirst());

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#projectExtensionDetails"
        )),
        summaryModel
    );
  }
}
