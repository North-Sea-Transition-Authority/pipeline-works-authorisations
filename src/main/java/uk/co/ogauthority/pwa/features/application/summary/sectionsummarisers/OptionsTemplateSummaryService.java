package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementRestController;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class OptionsTemplateSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public OptionsTemplateSummaryService(TaskListService taskListService,
                                       PadFileManagementService padFileManagementService) {
    this.taskListService = taskListService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.OPTIONS_TEMPLATE);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);

  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail, String templateName) {

    var templateFileViews = padFileManagementService
        .getUploadedFileViews(pwaApplicationDetail, FileDocumentType.OPTIONS_TEMPLATE);

    var fileView = !templateFileViews.isEmpty() ? templateFileViews.getFirst() : null;

    var sectionDisplayText = ApplicationTask.OPTIONS_TEMPLATE.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("optionsTemplateFileView", fileView);

    if (fileView != null) {
      summaryModel.put("optionsFileDownloadUrl", ReverseRouter.route(on(PadFileManagementRestController.class)
          .download(pwaApplicationDetail.getMasterPwaApplicationId(), UUID.fromString(fileView.getFileId()))));
    }

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#optionsTemplate"
        )),
        summaryModel
    );

  }

}
