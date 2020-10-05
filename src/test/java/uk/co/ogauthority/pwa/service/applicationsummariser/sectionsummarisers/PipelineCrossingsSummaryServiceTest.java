package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PipelineCrossingView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineCrossingsSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadPipelineCrossingService padPipelineCrossingService;

  @Mock
  private PadFileService padFileService;

  private PipelineCrossingsSummaryService pipelineCrossingsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    pipelineCrossingsSummaryService = new PipelineCrossingsSummaryService(padPipelineCrossingService, padFileService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(pipelineCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(pipelineCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(pipelineCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

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