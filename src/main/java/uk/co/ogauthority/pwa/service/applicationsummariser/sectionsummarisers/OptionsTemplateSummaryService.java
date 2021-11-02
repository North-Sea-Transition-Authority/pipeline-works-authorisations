package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.controller.OptionsTemplateController;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;

@Service
public class OptionsTemplateSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadFileService padFileService;

  @Autowired
  public OptionsTemplateSummaryService(TaskListService taskListService,
                                       PadFileService padFileService) {
    this.taskListService = taskListService;
    this.padFileService = padFileService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.OPTIONS_TEMPLATE);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);

  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail, String templateName) {

    var templateFileViews = padFileService
        .getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.OPTIONS_TEMPLATE, ApplicationFileLinkStatus.FULL);

    var fileView = !templateFileViews.isEmpty() ? templateFileViews.get(0) : null;

    var sectionDisplayText = ApplicationTask.OPTIONS_TEMPLATE.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("optionsTemplateFileView", fileView);
    summaryModel.put("optionsFileDownloadUrl", ReverseRouter.route(on(OptionsTemplateController.class)
        .handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(), null, null)));

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
