package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs.controller.SupplementaryDocumentsController;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class SupplementaryDocumentsSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadFileService padFileService;

  private SupplementaryDocumentsSummaryService supplementaryDocumentsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    supplementaryDocumentsSummaryService = new SupplementaryDocumentsSummaryService(
        taskListService,
        padFileService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);

  }

  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(supplementaryDocumentsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(supplementaryDocumentsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(supplementaryDocumentsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS, ApplicationFileLinkStatus.FULL)).thenReturn(
        List.of(
            new UploadedFileView("id", "name", 99L, "desc", Instant.now(), "#"),
            new UploadedFileView("id2", "name2", 100L, "desc2", Instant.now().minusSeconds(60), "#")
        ));

    var appSummary = supplementaryDocumentsSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).containsKey("docFileViews");

    @SuppressWarnings("unchecked")
    var fileViews = (List<UploadedFileView>) appSummary.getTemplateModel().get("docFileViews");
    assertThat(fileViews.size()).isEqualTo(2);

    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.SUPPLEMENTARY_DOCUMENTS.getDisplayName()));
    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.SUPPLEMENTARY_DOCUMENTS.getDisplayName(), "#supplementaryDocuments"));
    assertThat(appSummary.getTemplateModel()).containsEntry("suppDocFileDownloadUrl", ReverseRouter.route(
        on(SupplementaryDocumentsController.class).handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(), null, null)));

  }

}