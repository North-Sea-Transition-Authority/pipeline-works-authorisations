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
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossing;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PipelineCrossingView;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PipelineCrossingsSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadPipelineCrossingService padPipelineCrossingService;

  @Mock
  private PadFileService padFileService;

  private PipelineCrossingsSummaryService pipelineCrossingsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    pipelineCrossingsSummaryService = new PipelineCrossingsSummaryService(padPipelineCrossingService, padFileService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);

    pipelineCrossingsSummaryService.canSummarise(pwaApplicationDetail);

    verify(taskListService, times(1)).anyTaskShownForApplication(
        Set.of(ApplicationTask.CROSSING_AGREEMENTS), pwaApplicationDetail
    );

    verify(padPipelineCrossingService, times(1)).canShowInTaskList(pwaApplicationDetail);

  }


  @Test
  void canSummarise_whenHasCrossingsTaskShown_andPipelineCrossingSectionShown() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    when(padPipelineCrossingService.canShowInTaskList(any())).thenReturn(true);
    assertThat(pipelineCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenHasCrossingsTaskShown_andPipelineCrossingSectionNotShown() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    when(padPipelineCrossingService.canShowInTaskList(any())).thenReturn(false);
    assertThat(pipelineCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void canSummarise_whenCrossingTaskNotShown() {
    assertThat(pipelineCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    var padPipelineCrossing = new PadPipelineCrossing();
    padPipelineCrossing.setId(1);
    var pipelineCrossingViews = List.of(new PipelineCrossingView(padPipelineCrossing, List.of()));
    when(padPipelineCrossingService.getPipelineCrossingViews(pwaApplicationDetail)).thenReturn(pipelineCrossingViews);

    var fileView = new UploadedFileView(null, null, 1L, null, null, null);
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.PIPELINE_CROSSINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    var appSummary = pipelineCrossingsSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(4);
    assertThat(appSummary.getTemplateModel()).contains(entry("pipelineCrossingViews", pipelineCrossingViews));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", CrossingAgreementTask.PIPELINE_CROSSINGS.getDisplayText()));
    assertThat(appSummary.getTemplateModel()).contains(entry("pipelineCrossingFiles", List.of(fileView)));
    assertThat(appSummary.getTemplateModel()).containsKey("pipelineCrossingUrlFactory");

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(CrossingAgreementTask.PIPELINE_CROSSINGS.getDisplayText(), "#pipelineCrossingDetails")
    );

  }


}