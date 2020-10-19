package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.supplementarydocs.SupplementaryDocumentsController;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;

@Service
public class SupplementaryDocumentsSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadFileService padFileService;

  @Autowired
  public SupplementaryDocumentsSummaryService(TaskListService taskListService,
                                              PadFileService padFileService) {
    this.taskListService = taskListService;
    this.padFileService = padFileService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.SUPPLEMENTARY_DOCUMENTS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);

  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail, String templateName) {

    var docFileViews = padFileService
        .getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS, ApplicationFileLinkStatus.FULL)
        .stream()
        .sorted(Comparator.comparing(UploadedFileView::getFileName))
        .collect(Collectors.toList());

    var sectionDisplayText = ApplicationTask.SUPPLEMENTARY_DOCUMENTS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("docFileViews", docFileViews);
    summaryModel.put("suppDocFileDownloadUrl", ReverseRouter.route(on(SupplementaryDocumentsController.class)
        .handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(), null, null)));

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
