package uk.co.ogauthority.pwa.features.application.tasks.fasttrack;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers.FastTrackSummaryService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class FastTrackSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadFastTrackService padFastTrackService;

  private FastTrackSummaryService fastTrackSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    fastTrackSummaryService = new FastTrackSummaryService(padFastTrackService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(fastTrackSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(fastTrackSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(fastTrackSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    var fastTrackView = new FastTrackView(
        null, null, null,null, null, null, null, null);
    when(padFastTrackService.getFastTrackView(pwaApplicationDetail)).thenReturn(fastTrackView);

    var appSummary = fastTrackSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(2);
    assertThat(appSummary.getTemplateModel()).contains(entry("fastTrackView", fastTrackView));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.FAST_TRACK.getDisplayName()));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.FAST_TRACK.getDisplayName(), "#fastTrackDetails")
    );

  }


}