package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementRestController;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class SupplementaryDocumentsSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public SupplementaryDocumentsSummaryService(TaskListService taskListService,
                                              PadFileManagementService padFileManagementService) {
    this.taskListService = taskListService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.SUPPLEMENTARY_DOCUMENTS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);

  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail, String templateName) {

    var docFileViews = padFileManagementService
        .getUploadedFileViews(pwaApplicationDetail, FileDocumentType.SUPPLEMENTARY_DOCUMENTS)
        .stream()
        .sorted(Comparator.comparing(UploadedFileView::getFileName))
        .collect(Collectors.toList());

    var sectionDisplayText = ApplicationTask.SUPPLEMENTARY_DOCUMENTS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("docFileViews", docFileViews);
    summaryModel.put("suppDocFileDownloadUrl", ReverseRouter.route(
        on(PadFileManagementRestController.class).download(pwaApplicationDetail.getMasterPwaApplicationId(), null)));

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#supplementaryDocuments"
        )),
        summaryModel
    );

  }

}
