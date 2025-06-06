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
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.MedianLineAgreementView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class MedianLineAgreementSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadMedianLineAgreementService padMedianLineAgreementService;

  private MedianLineAgreementSummaryService medianLineAgreementSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    medianLineAgreementSummaryService = new MedianLineAgreementSummaryService(padMedianLineAgreementService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);

    medianLineAgreementSummaryService.canSummarise(pwaApplicationDetail);

    verify(taskListService, times(1)).anyTaskShownForApplication(
        Set.of(ApplicationTask.CROSSING_AGREEMENTS), pwaApplicationDetail
    );

    verify(padMedianLineAgreementService, times(1)).canShowInTaskList(pwaApplicationDetail);

  }

  @Test
  void canSummarise_whenHasCrossingsTaskShown_andMedianLineCrossingSectionShown() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    when(padMedianLineAgreementService.canShowInTaskList(any())).thenReturn(true);
    assertThat(medianLineAgreementSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenHasCrossingsTaskShown_andMedianLineCrossingSectionNotShown() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    when(padMedianLineAgreementService.canShowInTaskList(any())).thenReturn(false);
    assertThat(medianLineAgreementSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void canSummarise_whenCrossingTaskNotShown() {
    assertThat(medianLineAgreementSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    var fileView = UploadedFileViewTestUtil.createDefaultFileView();
    var medianLineAgreementView = new MedianLineAgreementView(null, null, null, List.of(fileView));
    when(padMedianLineAgreementService.getMedianLineCrossingView(pwaApplicationDetail)).thenReturn(medianLineAgreementView);

    var appSummary = medianLineAgreementSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(4);
    assertThat(appSummary.getTemplateModel()).contains(entry("medianLineAgreementView", medianLineAgreementView));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", CrossingAgreementTask.MEDIAN_LINE.getDisplayText()));
    assertThat(appSummary.getTemplateModel()).contains(entry("medianLineFiles", List.of(fileView)));
    assertThat(appSummary.getTemplateModel()).containsKey("medianLineUrlFactory");

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(CrossingAgreementTask.MEDIAN_LINE.getDisplayText(), "#medianLineAgreementDetails")
    );

  }


}