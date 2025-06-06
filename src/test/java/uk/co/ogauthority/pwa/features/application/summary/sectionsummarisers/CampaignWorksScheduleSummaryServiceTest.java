package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.WorkScheduleView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class CampaignWorksScheduleSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private CampaignWorksService campaignWorksService;

  private CampaignWorkScheduleSummaryService campaignWorkScheduleSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    campaignWorkScheduleSummaryService = new CampaignWorkScheduleSummaryService(campaignWorksService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(campaignWorkScheduleSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(campaignWorkScheduleSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(campaignWorkScheduleSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    var padCampaignWorkSchedule = new PadCampaignWorkSchedule();
    padCampaignWorkSchedule.setId(1);
    padCampaignWorkSchedule.setWorkFromDate(LocalDate.of(2020, 05, 01));
    padCampaignWorkSchedule.setWorkToDate(LocalDate.of(2020, 06, 01));
    var workSheduleView = new WorkScheduleView(padCampaignWorkSchedule, List.of());
    when(campaignWorksService.getWorkScheduleViews(pwaApplicationDetail)).thenReturn(List.of(workSheduleView));

    var appSummary = campaignWorkScheduleSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(2);
    assertThat(appSummary.getTemplateModel()).contains(entry("workScheduleViews", List.of(workSheduleView)));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.CAMPAIGN_WORKS.getDisplayName()));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.CAMPAIGN_WORKS.getDisplayName(), "#workScheduleDetails")
    );

  }


}