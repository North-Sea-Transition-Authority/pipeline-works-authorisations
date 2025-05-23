package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CableCrossingView;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossing;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossingService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class CableCrossingsSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadCableCrossingService padCableCrossingService;

  @Mock
  private PadFileManagementService padFileManagementService;

  private CableCrossingsSummaryService cableCrossingsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    cableCrossingsSummaryService = new CableCrossingsSummaryService(padCableCrossingService, taskListService, padFileManagementService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);

    cableCrossingsSummaryService.canSummarise(pwaApplicationDetail);

    verify(taskListService, times(1)).anyTaskShownForApplication(
        Set.of(ApplicationTask.CROSSING_AGREEMENTS), pwaApplicationDetail
    );

    verify(padCableCrossingService, times(1)).canShowInTaskList(pwaApplicationDetail);

  }

  @Test
  void canSummarise_whenHasCrossingsTaskShown_andCableCrossingSectionShown() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    when(padCableCrossingService.canShowInTaskList(any())).thenReturn(true);
    assertThat(cableCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenHasCrossingsTaskShown_andCableCrossingSectionNotShown() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    when(padCableCrossingService.canShowInTaskList(any())).thenReturn(false);
    assertThat(cableCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void canSummarise_whenCrossingTaskNotShown() {
    assertThat(cableCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    var cableCrossing = new PadCableCrossing();
    var cableCrossingViews = List.of(new CableCrossingView(cableCrossing));
    when(padCableCrossingService.getCableCrossingViews(pwaApplicationDetail)).thenReturn(cableCrossingViews);

    var fileView = new UploadedFileView(null, null, 1L, null, null, null);
    when(padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.CABLE_CROSSINGS)).thenReturn(List.of(fileView));

    var appSummary = cableCrossingsSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(4);
    assertThat(appSummary.getTemplateModel()).contains(entry("cableCrossingViews", cableCrossingViews));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", CrossingAgreementTask.CABLE_CROSSINGS.getDisplayText()));
    assertThat(appSummary.getTemplateModel()).contains(entry("cableCrossingFiles", List.of(fileView)));
    assertThat(appSummary.getTemplateModel()).containsKey("cableCrossingUrlFactory");

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(CrossingAgreementTask.CABLE_CROSSINGS.getDisplayText(), "#cableCrossingDetails")
    );

  }


}