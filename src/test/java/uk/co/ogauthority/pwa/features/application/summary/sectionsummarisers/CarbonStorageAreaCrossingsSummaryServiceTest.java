package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageAreaCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageCrossingView;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class CarbonStorageAreaCrossingsSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private CarbonStorageAreaCrossingService crossingService;

  @Mock
  private PadFileManagementService padFileManagementService;

  private CarbonStorageAreaCrossingSummaryService summaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    summaryService = new CarbonStorageAreaCrossingSummaryService(crossingService, taskListService, padFileManagementService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(summaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(summaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(summaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    var crossingView = new CarbonStorageCrossingView(1, null, null,  true);
    when(crossingService.getCrossedAreaViews(pwaApplicationDetail)).thenReturn(List.of(crossingView));

    var fileView = new UploadedFileView(null, null, 1L, null, null, null);
    when(padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.CARBON_STORAGE_CROSSINGS)).thenReturn(List.of(fileView));

    when(crossingService.isDocumentsRequired(pwaApplicationDetail)).thenReturn(true);

    var appSummary = summaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(5);
    assertThat(appSummary.getTemplateModel()).contains(entry("carbonStorageCrossingViews", List.of(crossingView)));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", CrossingAgreementTask.CARBON_STORAGE_AREAS.getDisplayText()));
    assertThat(appSummary.getTemplateModel()).contains(entry("carbonStorageCrossingFileViews", List.of(fileView)));
    assertThat(appSummary.getTemplateModel()).containsKey("carbonStorageCrossingUrlFactory");
    assertThat(appSummary.getTemplateModel()).contains(entry("isDocumentsRequired", true));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(CrossingAgreementTask.CARBON_STORAGE_AREAS.getDisplayText(), "#carbonStorageCrossingDetails")
    );

  }


}
