package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementRestController;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class SupplementaryDocumentsSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadFileManagementService padFileManagementService;

  private SupplementaryDocumentsSummaryService supplementaryDocumentsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    supplementaryDocumentsSummaryService = new SupplementaryDocumentsSummaryService(
        taskListService,
        padFileManagementService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);

  }

  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(supplementaryDocumentsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }


  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(supplementaryDocumentsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(supplementaryDocumentsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    when(padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.SUPPLEMENTARY_DOCUMENTS))
        .thenReturn(
            List.of(
                new UploadedFileView("id", "name", 99L, "desc", Instant.now(), "#"),
                new UploadedFileView("id2", "name2", 100L, "desc2", Instant.now().minusSeconds(60), "#")
            )
        );

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
        on(PadFileManagementRestController.class).download(pwaApplicationDetail.getMasterPwaApplicationId(), null)));
  }

}